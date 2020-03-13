package org.mulesoft.als.suggestions.plugins.aml.webapi

import amf.core.annotations.SynthesizedField
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.{EndPoint, Parameter, Server}
import org.mulesoft.als.suggestions.RawSuggestion
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin

import scala.concurrent.Future

trait UrlTemplateParam extends AMLCompletionPlugin {
  override def id: String = "UrlTemplateParam"

  override def resolve(request: AmlCompletionRequest): Future[Seq[RawSuggestion]] = {
    Future.successful {
      val params = request.amfObject match {
        case p: Parameter if p.binding.option().contains("path") && isName(request) =>
          request.branchStack.headOption match {
            case Some(e: EndPoint) => endpointParams(e)
            case Some(s: Server)   => serverParams(s)
            case _                 => Nil
          }
        case _ => Nil
      }
      params.map(toRaw)
    }
  }

  private def isName(request: AmlCompletionRequest) = request.fieldEntry.exists(_.field == ParameterModel.Name)

  private def endpointParams(e: EndPoint): Seq[String] = {
    e.parameters
      .filter(p => p.binding.option().contains("path") && p.annotations.contains(classOf[SynthesizedField]))
      .flatMap(_.name.option())
      .filter(n => e.path.option().getOrElse("").contains(s"{$n}"))
  }

  protected def toRaw(s: String): RawSuggestion = RawSuggestion.forObject(s, "parameters")

  protected def serverParams(server: Server): Seq[String] = {
    val url = server.url.option().getOrElse("")
    server.variables
      .flatMap(_.name.option())
      .filter(n => url.contains(s"{$n}"))
  }
}