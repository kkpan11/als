package org.mulesoft.als.suggestions.plugins.aml.webapi

import amf.core.model.domain.{AmfObject, Shape}
import amf.plugins.domain.webapi.models.Parameter
import org.mulesoft.als.common.YPartBranch
import org.mulesoft.als.suggestions.plugins.NonPatchHacks

trait WritingShapeInfo extends NonPatchHacks {
  protected def isWritingFacet(yPartBranch: YPartBranch, shape: Shape, stack: Seq[AmfObject]): Boolean =
    notValue(yPartBranch) && !yPartBranch.isKeyDescendantOf("required") && !writingShapeName(shape, yPartBranch) && !writingParamName(
      stack,
      yPartBranch) && !yPartBranch.parentEntryIs("properties")

  protected def writingShapeName(shape: Shape, yPartBranch: YPartBranch): Boolean =
    shape.name.value() == yPartBranch.stringValue

  protected def writingParamName(stack: Seq[AmfObject], yPartBranch: YPartBranch): Boolean =
    stack.headOption.exists {
      case p: Parameter => p.name.value() == yPartBranch.stringValue
      case _            => false
    }
}
