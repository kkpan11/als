package org.mulesoft.high.level.builder

import amf.core.annotations.SourceAST
import amf.core.metamodel.document.DocumentModel
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, ShapeModel}
import amf.core.metamodel.domain.extensions.{CustomDomainPropertyModel, PropertyShapeModel}
import amf.core.model.document.BaseUnit
import amf.core.model.domain._
import amf.core.metamodel.{Field, Obj, Type}
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.webapi.metamodel.{ParameterModel, PayloadModel}
import org.mulesoft.high.level.implementation._
import org.mulesoft.high.level.interfaces.{IASTUnit, IHighLevelNode, IParseResult}
import org.mulesoft.typesystem.nominal_interfaces.{IProperty, ITypeDefinition}
import org.mulesoft.typesystem.project.ITypeCollectionBundle
import org.mulesoft.typesystem.syaml.to.json.YJSONWrapper
import org.yaml.model.YPart

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

abstract class DefaultASTFactory extends IASTFactory {

    val matchers:mutable.Map[String,mutable.Map[String,IPropertyMatcher]]
            = mutable.Map[String,mutable.Map[String,IPropertyMatcher]]()

    val initializers:mutable.Map[String,mutable.Map[String,INodeInitializer]]
            = mutable.Map[String,mutable.Map[String,INodeInitializer]]()

    def registerPropertyMatcher(typeName:String,propertyName:String,matcher:IPropertyMatcher):Unit = {
        registerObject(typeName,propertyName,matcher,matchers)
    }

    def registerInitializer(typeName:String,propertyName:String,matcher:INodeInitializer):Unit = {
        registerObject(typeName,propertyName,matcher,initializers)
    }

    private def registerObject[T](
            typeName:String,
            propertyName:String,
            obj:T,
            map:mutable.Map[String,mutable.Map[String,T]]):Unit = {
        var mapOpt:Option[mutable.Map[String,T]] = map.get(typeName)
        mapOpt match {
            case Some(m) =>
            case None =>
                val m = mutable.Map[String, T]()
                map.put(typeName,m)
                mapOpt = Some(m)
        }
        mapOpt.get.put(propertyName,obj)
    }

    def registerPropertyMatcher(typeName:String,propertyName:String,field:Field):Unit = {
        registerPropertyMatcher(typeName,propertyName,FieldMatcher(field))
    }

    def getMatcherByTypeName(typeName:String, propertyName:String):Option[IPropertyMatcher]
        = getObjectByTypeName(typeName,propertyName,matchers)

    def getInitializerByTypeName(typeName:String, propertyName:String):Option[INodeInitializer]
        = getObjectByTypeName(typeName,propertyName,initializers)

    private def getObjectByTypeName[T](
            typeName:String,
            propertyName:String,
            map:mutable.Map[String,mutable.Map[String,T]]):Option[T]
        = map.get(typeName).flatMap(_.get(propertyName))

    def getMatcher(t:ITypeDefinition,propertyName:String):Option[IPropertyMatcher] = {
        getObject(t,propertyName,matchers)
    }

    def getInitializer(t:ITypeDefinition,propertyName:String):Option[INodeInitializer] = {
        getObject(t,propertyName,initializers)
    }

    private def getObject[T](
            t:ITypeDefinition,
            propertyName:String,
            map:mutable.Map[String,mutable.Map[String,T]]):Option[T] = {

        t.nameId.flatMap(getObjectByTypeName(_,propertyName,map)) match {
            case Some(matcher) => Some(matcher)
            case None =>
                var gotMatcher = false
                var result:Option[T] = None
                t.allSuperTypes.foreach(st => {
                    if(!gotMatcher){
                        st.nameId.flatMap(getObjectByTypeName(_,propertyName,map)) match {
                            case Some(matcher) =>
                                result = Some(matcher)
                                gotMatcher = true
                            case None =>
                        }
                    }
                })
                result
        }
    }

    override def getPropertyValues(node: IHighLevelNode, prop: IProperty, bundle:ITypeCollectionBundle):Seq[BasicASTNode]
        = getPropertyValues(node,node.amfNode,node.amfBaseUnit,prop,bundle)

    override def getPropertyValues(node:IHighLevelNode, amfNode:AmfObject, baseUnit:BaseUnit, prop:IProperty, bundle:ITypeCollectionBundle): Seq[BasicASTNode] = {

        var resultOpt = for {
            propName <- prop.nameId
            matcher <- getMatcher(node.definition,propName)
        }
        yield {
            var propertyMatches = matcher.operate(amfNode,node)
            var range:ITypeDefinition = prop.range.get
            for{
                array <- range.array
                ct <- array.componentType
            } yield{
                range = ct
            }

            propertyMatches.flatMap({
                case am: AttributeMatchResult =>
                    val attr = ASTPropImpl(
                        am.node, baseUnit, Option(node), discriminate(range, am.node, None), Option(prop), am.buffer)
                    Some(attr)
                case em: ElementMatchResult =>
                    val nominalType = determineUserType(em.node, Option(prop), Option(node), bundle)
                    var hlNode = ASTNodeImpl(
                    em.node, baseUnit, Option(node), discriminate(range,em.node,nominalType), Option(prop))
                    em.yamlNode.foreach(x=>hlNode.sourceInfo.withSources(List(x)))
                    nominalType.foreach(hlNode.setLocalType)
                    Some(hlNode)
                case _ => None
            })
        }
        resultOpt.getOrElse(Seq())
    }

    def newChild(node:IHighLevelNode, prop:IProperty, typeHint:Option[ITypeDefinition] = None):Option[BasicASTNode] = {

        var baseUnit = node.amfBaseUnit
        var bundle = node.astUnit.project.types
        var resultOpt = for {
            propName <- prop.nameId
            matcher <- getMatcher(node.definition, propName)
        }
        yield {
            var cfg = NodeCreationConfig(node.amfNode, node, prop, this, typeHint)
            var propertyMatches = matcher.appendNewValue(cfg)
            var range: ITypeDefinition = prop.range.get
            for {
                array <- range.array
                ct <- array.componentType
            } yield {
                range = ct
            }

            propertyMatches.flatMap({
                case am: AttributeMatchResult =>
                    val attr = ASTPropImpl(
                        am.node, baseUnit, Option(node), discriminate(range, am.node, None), Option(prop), am.buffer)
                    Some(attr)
                case em: ElementMatchResult =>
                    val nominalType = determineUserType(em.node, Option(prop), Option(node), bundle)
                    var hlNode = ASTNodeImpl(
                        em.node, baseUnit, Option(node), discriminate(range, em.node, nominalType), Option(prop))
                    initNode(hlNode)
                    em.yamlNode.foreach(x => hlNode.sourceInfo.withSources(List(x)))
                    nominalType.foreach(hlNode.setLocalType)
                    Some(hlNode)
                case fmr: FieldOwnerMatchResult =>
                    val fieldType = fmr.fMatcher.field.`type`
                    var isArray = fieldType.isInstanceOf[amf.core.metamodel.Type.Array]
                    var actualType = if(isArray) fieldType.asInstanceOf[amf.core.metamodel.Type.Array].element else fieldType

                    var instanceOpt:Option[AmfObject] = None
                    actualType match {
                        case sm:ShapeModel =>
                            val instance = AnyShapeModel.modelInstance
                            instanceOpt = Option(instance)
                        case _ =>
                    }
                    instanceOpt match {
                        case Some(x) =>
                            fmr.fMatcher.appendValueToAMFModel(fmr.node,x)
                            Some(ElementMatchResult(x))
                        case _ => None
                    }
                    val newNode = instanceOpt.get
                    val nominalType = determineUserType(newNode, Option(prop), Option(node), bundle)
                    var hlNode = ASTNodeImpl(
                        newNode, baseUnit, Option(node), discriminate(range, newNode, nominalType), Option(prop))
                    //em.yamlNode.foreach(x => hlNode.sourceInfo.withSources(List(x)))
                    nominalType.foreach(hlNode.setLocalType)
                    initNode(hlNode)
                    Some(hlNode)
                case _ => None
            })
        }
        resultOpt.flatten
    }

    def initNode(hlNode:IHighLevelNode):Unit = {
        if(hlNode.property.isDefined && hlNode.parent.isDefined) {
            var propName = hlNode.property.get.nameId.get
            getInitializer(hlNode.parent.get.definition,propName).foreach(_.initNode(hlNode))
        }
    }


    def determineUnit(obj:AmfObject, unit:IASTUnit):IASTUnit = {

        var id = obj.id
        var ind = id.indexOf("#")
        if(ind<0){
            ind = id.length
        }

        unit
    }
}

object DefaultASTFactory {

    def extractShape(obj: AmfObject): Option[Shape] = {

        var dElementOpt:Option[DomainElement] = obj match {
            case bu:BaseUnit => Option(bu.fields.getValue(DocumentModel.Encodes)).map(_.value).map({
                case de:DomainElement => de
                case _ => null
            })
            case de:DomainElement => Some(de)
            case _ => None
        }

        if(dElementOpt.isEmpty){
            None
        }
        else {
            var dElement = dElementOpt.get
            var shapeOpt: Option[Shape] = dElement.meta match {
                case PropertyShapeModel => Option(dElement.fields.get(PropertyShapeModel.Range)
                    .asInstanceOf[Shape])
                case CustomDomainPropertyModel => Option(dElement.fields.get(CustomDomainPropertyModel.Schema).asInstanceOf[Shape])
                case PayloadModel => Option(dElement.fields.get(PayloadModel.Schema).asInstanceOf[Shape])
                case ParameterModel => Option(dElement.fields.get(ParameterModel.Schema).asInstanceOf[Shape])
                case shapeModel: ShapeModel => Some(dElement.asInstanceOf[Shape])
                case _ => None
            }
            shapeOpt
        }
    }
}

sealed abstract class MatchResult(val node:AmfObject)

class AttributeMatchResult(node:AmfObject, val buffer:IValueBuffer) extends MatchResult(node) {}

object AttributeMatchResult{
    def apply(node:AmfObject, buffer:IValueBuffer):AttributeMatchResult = new AttributeMatchResult(node,buffer)
}

class ElementMatchResult(node:AmfObject,val yamlNode:Option[YPart]) extends MatchResult(node) {}

object ElementMatchResult{
    def apply(node:AmfObject):ElementMatchResult
            = new ElementMatchResult(node,node.annotations.find(classOf[SourceAST]).map(_.ast))

    def apply(node:AmfObject,yamlNodeOpt:Option[YPart]):ElementMatchResult
            = new ElementMatchResult(node,yamlNodeOpt)
}

class FieldMatchResult(node:AmfObject, val value:AmfElement, val fMatcher:FieldMatcher) extends MatchResult(node) {}

object FieldMatchResult {
    def apply(node:AmfObject, value:AmfElement, fMatcher:FieldMatcher): FieldMatchResult = new FieldMatchResult(node,value,fMatcher)
}

class FieldOwnerMatchResult(node:AmfObject, val fMatcher:FieldMatcher) extends MatchResult(node) {}

object FieldOwnerMatchResult {
    def apply(fieldOwner: AmfObject, fMatcher: FieldMatcher): FieldOwnerMatchResult = new FieldOwnerMatchResult(fieldOwner, fMatcher)
}

trait INodeInitializer {

    def initNode(node:IParseResult)
}

trait IPropertyMatcher {

    var bufferConstructor:Option[(AmfObject,IHighLevelNode)=>IValueBuffer] = None

    var yamlPath:Option[Seq[String]] = None

    def withCustomBuffer(bufConstructor:(AmfObject,IHighLevelNode)=>IValueBuffer):IPropertyMatcher = {
        bufferConstructor = Option(bufConstructor)
        this
    }

    def withYamlPath(yp:Seq[String]):IPropertyMatcher = {
        yamlPath = Option(yp)
        this
    }

    def withYamlPath(yp:String):IPropertyMatcher = withYamlPath(List(yp))

    var STRICT:Boolean = true

    def operate(obj:AmfObject, hlNode:IHighLevelNode):Seq[MatchResult] = {
        var result = doOperate(obj,hlNode)
        bufferConstructor match {
            case Some(c) => result = result.map(mr=>AttributeMatchResult(mr.node,c(mr.node,hlNode)))
            case None =>
        }
        yamlPath match {
            case Some(yp) =>
                if(yp.lengthCompare(1)==0) {
                    var key = yp.head
                    result = result.flatMap({
                        case er: ElementMatchResult =>
                            var srcOpt = er.yamlNode.flatMap(YJSONWrapper(_))
                                .flatMap(_.propertyValue(key)).map(_.source)
                            srcOpt match {
                                case Some(yn) => Some(ElementMatchResult(er.node,srcOpt))
                                case None => None
                            }

                        case mr: MatchResult => Some(mr)
                    })
                }
            case None =>
        }
        result
    }

    def doOperate(obj:AmfObject, hlNode:IHighLevelNode):Seq[MatchResult]

    def appendNewValue(cfg:NodeCreationConfig):Option[MatchResult] = {
        var result = doAppendNewValue(cfg)
        bufferConstructor match {
            case Some(c) => result = result.map({
                case fmr:FieldMatchResult => AttributeMatchResult(fmr.node,c(fmr.value.asInstanceOf[AmfObject],cfg.hlNode))
                case mr:MatchResult => AttributeMatchResult(mr.node,c(mr.node,cfg.hlNode))
            })
            case None =>
        }
        result
    }

    def appendNewValue(mr:MatchResult, cfg:NodeCreationConfig):Option[MatchResult]
        = {
        mr match {
            case emr:ElementMatchResult =>
                var newCfg = cfg.newConfigWithObject(emr.node)
                appendNewValue(newCfg)
            case fmr:FieldMatchResult =>
                var newCfg = cfg.newConfigWithObject(fmr.node)
                appendNewValue(newCfg)
            case _ => None
        }
    }

    def doAppendNewValue(cfg:NodeCreationConfig):Option[MatchResult]

    def + (that:IPropertyMatcher):IPropertyMatcher = MatchersChain(this,that)
    def + (field:Field):IPropertyMatcher = this + FieldMatcher(field)
    def * (that:IPropertyMatcher):IPropertyMatcher = CompositeMatcher(this,that)
    def * (field:Field):IPropertyMatcher = this * FieldMatcher(field)
    def ifType(model:Obj):IPropertyMatcher = MatchersChain(this,TypeFilter(model,STRICT))
    def ifSubtype(model:Obj):IPropertyMatcher = MatchersChain(this,TypeFilter(model))
    def & (that:IPropertyMatcher):IPropertyMatcher = AndMatcher(List(this,that))
    def & (field:Field):IPropertyMatcher = AndMatcher(List(this,FieldMatcher(field)))
    def | (that:IPropertyMatcher):IPropertyMatcher = OrMatcher(List(this,that))
    def | (field:Field):IPropertyMatcher = OrMatcher(List(this,FieldMatcher(field)))
}

class ThisMatcher extends IPropertyMatcher {

    override def doOperate(obj: AmfObject, hlNode: IHighLevelNode): Seq[MatchResult] = List(ElementMatchResult(obj))

    override def + (that:IPropertyMatcher):IPropertyMatcher = that.withCustomBuffer(this.bufferConstructor.orNull)

    override def doAppendNewValue(cfg:NodeCreationConfig):Option[MatchResult] = Some(ElementMatchResult(cfg.obj))
}

object ThisMatcher {
    def apply():ThisMatcher = new ThisMatcher()
}

class BaseUnitMatcher extends IPropertyMatcher {

    override def doOperate(obj: AmfObject, hlNode:IHighLevelNode): Seq[MatchResult] = List(ElementMatchResult(hlNode.amfBaseUnit))

    override def doAppendNewValue(cfg:NodeCreationConfig):Option[MatchResult] = Some(ElementMatchResult(cfg.hlNode.amfBaseUnit))
}

object BaseUnitMatcher {
    def apply():BaseUnitMatcher = new BaseUnitMatcher()
}

class ParentMatcher extends IPropertyMatcher {

    override def doOperate(obj: AmfObject, hlNode: IHighLevelNode): Seq[MatchResult] =
        hlNode.parent match {
            case Some(p) => List(ElementMatchResult(p.amfNode))
            case None => Seq()
        }

    override def doAppendNewValue(cfg:NodeCreationConfig):Option[MatchResult] = None
}

object ParentMatcher {
    def apply():ParentMatcher = new ParentMatcher()
}

class TypeFilter(_clazz:Obj,strict:Boolean) extends IPropertyMatcher {

    val thisIds:Seq[String] = _clazz.`type`.map(_.name)

    override def doOperate(obj: AmfObject, hlNode:IHighLevelNode): Seq[MatchResult] = {
        obj match {
            case de:DomainElement =>
                var thatIds:List[String] = obj.asInstanceOf[DomainElement].meta.`type`.map(_.name)
                if((!strict||thisIds.lengthCompare(thatIds.length)==0)
                    &&thisIds.forall(thatIds.contains(_))){
                    List(ElementMatchResult(obj))
                }
                else Seq()
            case _ => Seq()
        }
    }

    override def doAppendNewValue(cfg:NodeCreationConfig):Option[MatchResult] = doOperate(cfg.obj,cfg.hlNode).headOption

    override def appendNewValue(mr:MatchResult, cfg:NodeCreationConfig):Option[MatchResult] = mr match {

        case fmr: FieldOwnerMatchResult =>
            var instanceOpt:Option[AmfObject] = None
            _clazz match {
                case sm:ShapeModel =>
                    val instance = AnyShapeModel.modelInstance
                    instanceOpt = Option(instance)
                case de: DomainElementModel =>
                    try {
                        val instance = de.modelInstance
                        instanceOpt = Option(instance)
                    }
                    catch {
                        case x: Throwable =>
                    }
                case _ =>
            }
            instanceOpt match {
                case Some(x) =>
                    fmr.fMatcher.appendValueToAMFModel(fmr.node,x)
                    Some(ElementMatchResult(x))
                case _ => None
            }
        case emr:ElementMatchResult =>
            var newCfg = cfg.newConfigWithObject(emr.node)
            appendNewValue(newCfg)
        case _ => None
    }
}

object TypeFilter {
    def apply(t:Obj):TypeFilter = new TypeFilter(t,false)
    def apply(t:Obj,strict:Boolean):TypeFilter = new TypeFilter(t,strict)
}

/**
  * Only applicable to scalars
  */
class AndMatcher(matchers:Seq[IPropertyMatcher]) extends IPropertyMatcher {

    override def doOperate(obj: AmfObject, hlNode:IHighLevelNode): Seq[MatchResult] = {

        if(matchers.isEmpty){
            Seq()
        }
        else{
            var table:Seq[ListBuffer[MatchResult]]
                = matchers.map(ListBuffer[MatchResult]()++=_.operate(obj,hlNode))
            var size = table.foldLeft(0){(x,i) => math.max(x,i.length)}
            var result: ListBuffer[MatchResult] = ListBuffer()
            for(i<-0 to size){
                var arr:ListBuffer[AttributeMatchResult] = ListBuffer()
                table.filter(_.lengthCompare(i)>0).foreach(_(i) match {
                    case amr : AttributeMatchResult => arr += amr
                    case _ =>
                })
                arr.length match {
                    case 0 =>
                    case 1 => result += arr.head
                    case _ => result += AttributeMatchResult(arr.head.node,CompositeValueBuffer(arr.map(_.buffer)))
                }
            }
            result
        }
    }

    override def doAppendNewValue(cfg:NodeCreationConfig):Option[MatchResult] = {
        if(matchers.isEmpty){
            None
        }
        else{
            var results:Seq[AttributeMatchResult]
                = matchers.flatMap(_.appendNewValue(cfg)).filter(_.isInstanceOf[AttributeMatchResult]).map(_.asInstanceOf[AttributeMatchResult])

            var result: Option[AttributeMatchResult] = results.length match {
                case 0 => None
                case 1 => results.headOption
                case _ => Some(AttributeMatchResult(results.head.node,CompositeValueBuffer(results.map(_.buffer))))
            }
            result
        }
    }
}

object AndMatcher {
    def apply(matchers:Seq[IPropertyMatcher]):AndMatcher = new AndMatcher(matchers)
}

class OrMatcher(matchers:Seq[IPropertyMatcher]) extends IPropertyMatcher {

    override def doOperate(obj: AmfObject, hlNode:IHighLevelNode): Seq[MatchResult] = {
        var result:Seq[MatchResult] = Seq()
        matchers.find(x=>{
            result = x.operate(obj,hlNode)
            result.nonEmpty
        })
        result
    }

    override def doAppendNewValue(cfg:NodeCreationConfig):Option[MatchResult] = {
        var result:Option[MatchResult] = None
        matchers.find(x=>{
            result = x.appendNewValue(cfg)
            result.nonEmpty
        })
        result
    }
}

object OrMatcher {
    def apply(matchers:Seq[IPropertyMatcher]):OrMatcher = new OrMatcher(matchers)
}


class MatchersChain(left: IPropertyMatcher, right: IPropertyMatcher) extends IPropertyMatcher {

    override def doOperate(obj: AmfObject, hlNode:IHighLevelNode): Seq[MatchResult] = {
        var result: ListBuffer[MatchResult] = ListBuffer()
        left.operate(obj,hlNode).foreach({
            case em: ElementMatchResult => result ++= right.operate(em.node,hlNode)
            case _ =>
        })
        result
    }

    override def doAppendNewValue(cfg:NodeCreationConfig): Option[MatchResult] = {
        left.appendNewValue(cfg).flatMap(mr=>right.appendNewValue(mr,cfg))
    }
}

object MatchersChain {
    def apply(left: IPropertyMatcher, right: IPropertyMatcher):MatchersChain = new MatchersChain(left,right)
}

class CompositeMatcher(first: IPropertyMatcher, second: IPropertyMatcher) extends IPropertyMatcher {

    override def doOperate(obj: AmfObject, hlNode:IHighLevelNode): Seq[MatchResult] = {
        var result:ListBuffer[MatchResult] = ListBuffer()
        result ++= first.operate(obj,hlNode)
        result ++= second.operate(obj,hlNode)
        result
    }

    override def doAppendNewValue(cfg:NodeCreationConfig):Option[MatchResult] = {

        cfg.typeHint match {
            case Some(hint) =>

                var newCfg = cfg.cloneWithNoAppend
                var res1 = first.appendNewValue(newCfg)
                var res2 = second.appendNewValue(newCfg)
                var results = (res1 :: res2 :: Nil).flatten

                var rangeOpt = cfg.prop.range
                var names:mutable.Set[String] = mutable.Set()
                while(rangeOpt.isDefined
                    && rangeOpt.get.isArray
                    && !names.contains(rangeOpt.get.nameId.getOrElse(""))){
                    names.add(rangeOpt.get.nameId.getOrElse(""))
                    rangeOpt = rangeOpt.get.array.get.componentType
                }

                rangeOpt match {
                    case Some(r) =>
                        if(res1.exists(mr => cfg.factory.discriminate(r, mr.node, None) == hint)){
                            if(cfg.appendValueToTree){
                                first.appendNewValue(cfg)
                            }
                            else {
                                res1
                            }
                        }
                        else if(res2.exists(mr => cfg.factory.discriminate(r, mr.node, None) == hint)){
                            if(cfg.appendValueToTree){
                                second.appendNewValue(cfg)
                            }
                            else {
                                res2
                            }
                        }
                        else {
                            res1
                        }
                    case None => None
                }
            case None =>
                first.appendNewValue(cfg).orElse(second.appendNewValue(cfg))
        }
    }
}

object CompositeMatcher {
    def apply(left: IPropertyMatcher, right: IPropertyMatcher):CompositeMatcher = new CompositeMatcher(left,right)
}

class FieldMatcher(val field: Field) extends IPropertyMatcher {

    override def doOperate(obj: AmfObject, hlNode: IHighLevelNode): Seq[MatchResult] = {
        var result: ListBuffer[MatchResult] = ListBuffer()
        obj.fields.get(field) match {
            case dataNode:DataNode =>
                dataNode.annotations.find(classOf[SourceAST]) match {
                    case Some(yn) => result += AttributeMatchResult(
                        obj, JSONValueBuffer(obj,hlNode,YJSONWrapper(yn.ast)))
                    case _ =>
                }

            case amfObj: AmfObject => result += ElementMatchResult(amfObj)
            case scalar: AmfScalar => result += AttributeMatchResult(obj, bufferConstructor match {
                    case Some(c) => c.apply(obj,hlNode)
                    case _ => BasicValueBuffer(obj, field)
                })
            case array: AmfArray =>
                var i = 0
                array.values.foreach(x => {
                    x match {
                        case sn: ScalarNode => result += AttributeMatchResult(obj,BasicValueBuffer(obj, field, i))
                        case amfObj: AmfObject => result += ElementMatchResult(amfObj)
                        case scalar: AmfScalar => result += AttributeMatchResult(obj, BasicValueBuffer(obj, field, i))
                        case _ =>
                    }
                    i += 1
                })
            case _ =>
        }
        result
    }

    override def appendNewValue(mr:MatchResult, cfg:NodeCreationConfig):Option[MatchResult]
    = {
        mr match {
            case fmr:FieldMatchResult => fmr.value match {
                case obj:AmfObject => appendNewValue(cfg.newConfigWithObject(obj))
                case _ => None
            }
            case _ => super.appendNewValue(mr,cfg)
        }
    }

    override def doAppendNewValue(cfg:NodeCreationConfig): Option[MatchResult] = {
        var result: ListBuffer[MatchResult] = ListBuffer()
        val fieldType = field.`type`
        var isArray = fieldType.isInstanceOf[amf.core.metamodel.Type.Array]
        var actualType = if(isArray) fieldType.asInstanceOf[amf.core.metamodel.Type.Array].element else fieldType

        val currentValueOpt = Option(cfg.obj.fields.get(field))
        if(!isArray && currentValueOpt.nonEmpty){
            currentValueOpt.map(x=>FieldMatchResult(cfg.obj,x,this))
        }
        else {
            var index = 0
            var arrOpt:Option[AmfArray] = None
            if(isArray && currentValueOpt.nonEmpty){
                currentValueOpt.get match {
                    case arr:AmfArray =>
                        arrOpt = Some(arr)
                        index = arr.values.length
                    case _ =>
                }
            }
            var valueObjOpt:Option[AmfElement] = None
            var matchResultOpt:Option[MatchResult] = None
            actualType match {
                case sc:Type.Scalar =>
                    valueObjOpt = Some(AmfScalar(null))
                    var buffer =
                        if(isArray)
                            BasicValueBuffer(cfg.obj,field,index)
                        else
                            BasicValueBuffer(cfg.obj,field)
                    matchResultOpt = Some(AttributeMatchResult(cfg.obj,buffer))
                case DataNodeModel =>
                    valueObjOpt = Some(null)
                    var buffer =
                        if(isArray)
                            BasicValueBuffer(cfg.obj,field,index)
                        else
                            BasicValueBuffer(cfg.obj,field)
                    matchResultOpt = Some(AttributeMatchResult(cfg.obj,buffer))
                case dem:DomainElementModel =>
                    try {
                        val instance = dem.modelInstance
                        valueObjOpt = Option(instance)
                        matchResultOpt = Some(ElementMatchResult(instance))
                    }
                    catch{
                        case x:Throwable =>
                            dem match {
                                case ShapeModel =>
                                    val instance = AnyShapeModel.modelInstance
                                    valueObjOpt = Option(instance)
                                    matchResultOpt = Some(ElementMatchResult(instance))
                                case _ =>
                                    matchResultOpt = Some(FieldOwnerMatchResult(cfg.obj,this))
                            }
                    }
                case _ =>
            }
            if(cfg.appendValueToTree) {
                valueObjOpt.foreach(x => appendValueToAMFModel(cfg.obj, x))
            }
            matchResultOpt
        }
    }

    def appendValueToAMFModel(obj: AmfObject, valueObj: AmfElement): Unit = {
        val fieldType = field.`type`
        var isArray = fieldType.isInstanceOf[amf.core.metamodel.Type.Array]
        val currentValueOpt = Option(obj.fields.get(field))
        var arrOpt:Option[AmfArray] = None
        valueObj match {
            case o:AmfObject => o.withId(obj.id + "/" + field + "/new-node")
            case _ =>
        }
        if(isArray && currentValueOpt.nonEmpty){
            currentValueOpt.get match {
                case arr:AmfArray =>
                    arrOpt = Some(arr)
                case _ =>
            }
        }
        if (isArray) {
            var existingArr: Seq[AmfElement] =
                if (arrOpt.nonEmpty)
                    arrOpt.get.values
                else List()

            var newArr: ListBuffer[AmfElement] = ListBuffer() ++= existingArr += valueObj

            var newAmfArray = AmfArray(newArr)
            obj.fields.setWithoutId(field, newAmfArray)
        }
        else {
            obj.fields.setWithoutId(field, valueObj)
        }
    }
}

object FieldMatcher {
    def apply(field: Field) = new FieldMatcher(field)
}

class NodeCreationConfig(
    val obj: AmfObject,
    val hlNode: IHighLevelNode,
    val prop: IProperty,
    val factory: IASTFactory,
    val typeHint:Option[ITypeDefinition] = None){

    private var _appendValueToTree:Boolean = true

    def appendValueToTree:Boolean = _appendValueToTree

    def newConfigWithObject(o:AmfObject):NodeCreationConfig = new NodeCreationConfig(o,hlNode,prop,factory,typeHint)

    def cloneWithNoAppend:NodeCreationConfig = {
        var result = new NodeCreationConfig(obj,hlNode,prop,factory,typeHint)
        result._appendValueToTree = false
        result
    }
}

object NodeCreationConfig {
    def apply(obj: AmfObject, hlNode: IHighLevelNode, prop: IProperty, factory: IASTFactory, typeHint: Option[ITypeDefinition] = None): NodeCreationConfig = new NodeCreationConfig(obj, hlNode, prop, factory, typeHint)
}