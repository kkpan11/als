package org.mulesoft.als.server.modules.workspace.references.visitors.noderelationship.plugins

import amf.core.model.document.BaseUnit
import amf.core.model.domain.AmfElement
import org.mulesoft.als.actions.common.RelationshipLink
import org.mulesoft.als.server.modules.workspace.references.visitors.AmfElementVisitorFactory
import org.mulesoft.als.server.modules.workspace.references.visitors.noderelationship.NodeRelationshipVisitorType
import org.mulesoft.amfintegration.AmfImplicits._

class ExternalNodeReferenceVisitor() extends NodeRelationshipVisitorType {
  override protected def innerVisit(element: AmfElement): Seq[RelationshipLink] =
    element.annotations.externalJsonSchemaShape
      .flatMap { originalEntry =>
        element.annotations
          .ast()
          .map { (originalEntry, _) }
      }
      .map { t =>
        RelationshipLink(t._1.value, t._2, getName(element))
      }
      .toSeq
}

object ExternalNodeReferenceVisitor extends AmfElementVisitorFactory {
  override def apply(bu: BaseUnit): Option[ExternalNodeReferenceVisitor] =
    if (applies(bu))
      Some(new ExternalNodeReferenceVisitor())
    else None
}
