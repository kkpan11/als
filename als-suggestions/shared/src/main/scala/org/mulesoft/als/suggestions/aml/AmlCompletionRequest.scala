package org.mulesoft.als.suggestions.aml

import amf.core.annotations.LexicalInformation
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfArray, AmfObject, DomainElement}
import amf.core.parser.FieldEntry
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, PropertyMapping}
import org.mulesoft.als.common.AmfSonElementFinder._
import org.mulesoft.als.common.dtoTypes.Position
import org.mulesoft.als.suggestions.interfaces.CompletionRequest

class AmlCompletionRequest(override val baseUnit: BaseUnit,
                           override val position: Position,
                           override val actualDialect: Dialect)
    extends CompletionRequest {

  private lazy val amfObject = baseUnit.findSon(position)

  override lazy val fieldEntry: Option[FieldEntry] = {
    amfObject.fields
      .fields()
      .find(f =>
        f.value.value match {
          case _: AmfArray =>
            f.value.annotations
              .find(classOf[LexicalInformation])
              .exists(_.contains(position)) && f.value.annotations
              .find(classOf[LexicalInformation])
              .forall(_.containsCompletly(position))
          case v =>
            v.position()
              .exists(_.contains(position)) &&
              f.value.annotations
                .find(classOf[LexicalInformation])
                .forall(_.containsCompletly(position))
      })
  }

  override val propertyMapping: Seq[PropertyMapping] = {
    val mappings = getDialectNode(actualDialect, amfObject, fieldEntry) match {
      case Some(nm: NodeMapping) => nm.propertiesMapping()
      case _                     => Nil
    }
    fieldEntry match {
      case Some(e) =>
        if (e.value.value
              .position()
              .exists(li => li.contains(position)))
          mappings
            .find(
              pm =>
                pm.fields
                  .fields()
                  .exists(f => f.value.toString == e.field.value.iri()))
            .map(Seq(_))
            .getOrElse(Nil)
        else mappings
      case _ => mappings
    }
  }

  private def getDialectNode(dialect: Dialect,
                             amfObject: AmfObject,
                             fieldEntry: Option[FieldEntry]): Option[DomainElement] =
    dialect.declares.find {
      case s: NodeMapping =>
        s.nodetypeMapping.value() == amfObject.meta.`type`.head.iri() &&
          fieldEntry.forall(f => {
            s.propertiesMapping()
              .find(
                pm =>
                  pm.fields
                    .fields()
                    .exists(_.value.toString == f.field.value.iri()))
              .exists(_.mapTermKeyProperty().isNullOrEmpty)
          })
      case _ => false
    }
}

object AmlCompletionRequest {
  def apply(amfPosition: Position, bu: BaseUnit, dialect: Dialect): AmlCompletionRequest =
    new AmlCompletionRequest(bu, amfPosition, dialect)
}
