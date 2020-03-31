package org.mulesoft.amfintegration.dialect.dialects.metadialect

import amf.core.vocabulary.Namespace.XsdTypes._
import amf.dialects.oas.nodes.DialectNode
import amf.plugins.document.vocabularies.metamodel.document.DialectModel
import amf.plugins.document.vocabularies.metamodel.domain.{
  NodeMappingModel,
  PropertyMappingModel,
  UnionNodeMappingModel
}
import amf.plugins.document.vocabularies.model.domain.{PropertyMapping, UnionNodeMapping}

object NodeMappingObjectNode extends DialectNode {
  override def name: String = "NodeMappingObjectNode"

  override def nodeTypeMapping: String = NodeMappingModel.`type`.head.iri()

  override def properties: Seq[PropertyMapping] = Seq(
    PropertyMapping()
      .withId(location + s"#/declarations/$name/classTerm")
      .withNodePropertyMapping(NodeMappingModel.NodeTypeMapping.value.iri())
      .withName("classTerm")
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(location + s"#/declarations/$name/patch")
      .withNodePropertyMapping(NodeMappingModel.MergePolicy.value.iri())
      .withName("patch")
      .withLiteralRange(xsdString.iri())
      .withEnum(
        Seq(
          "insert",
          "delete",
          "update",
          "upsert",
          "ignore",
          "fail"
        )),
    PropertyMapping()
      .withId(location + s"#/declarations/$name/mappings")
      .withNodePropertyMapping(NodeMappingModel.PropertiesMapping.value.iri())
      .withName("mappings")
      .withObjectRange(Seq(PropertyMappingObjectNode.id))
      .withMapTermKeyProperty(PropertyMappingModel.Name.value.iri()),
    PropertyMapping()
      .withId(location + s"#/declarations/$name/extends")
      .withNodePropertyMapping(NodeMappingModel.Extends.value.iri())
      .withName("extends")
      .withLiteralRange(xsdUri.iri()),
    PropertyMapping()
      .withId(location + s"#/declarations/$name/idTemplate")
      .withNodePropertyMapping(NodeMappingModel.IdTemplate.value.iri())
      .withName("idTemplate")
      .withLiteralRange(xsdUri.iri()),
    PropertyMapping()
      .withId(location + s"#/declarations/$name/union") // not sure, just to be suggested when empty
      .withNodePropertyMapping(UnionNodeMappingModel.ObjectRange.value.iri())
      .withName("union")
      .withLiteralRange(xsdUri.iri())
      .withAllowMultiple(true)
  )
}
