package org.mulesoft.als.suggestions.aml.webapi

import amf.dialects.RAML08Dialect
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.als.suggestions.plugins.aml.{ResolveDefault, StructureCompletionPlugin}
import org.mulesoft.als.suggestions.plugins.aml.webapi.raml._
import org.mulesoft.als.suggestions.plugins.aml.webapi.raml.raml08._
import org.mulesoft.als.suggestions.plugins.aml.webapi.raml.raml08.structure.ResolvePayload
import org.mulesoft.als.suggestions.plugins.aml.webapi.raml.raml10.structure.ResolveShapeAndSecurity
import org.mulesoft.als.suggestions.plugins.aml.webapi.{
  ObjectExamplePropertiesCompletionPlugin,
  SecuredByCompletionPlugin
}
import org.mulesoft.als.suggestions.{AMLBaseCompletionPlugins, CompletionsPluginHandler}

object Raml08CompletionPluginRegistry {

  private val all: Seq[AMLCompletionPlugin] =
    AMLBaseCompletionPlugins.all :+
      StructureCompletionPlugin(List(
        ResolveShapeAndSecurity,
        ResolvePayload,
        ResolveDefault
      )) :+
      Raml08ParamsCompletionPlugin :+
      Raml08BooleanPropertyValue :+
      Raml08TypeFacetsCompletionPlugin :+
      RamlTypeDeclarationReferenceCompletionPlugin :+
      RamlCustomFacetsCompletionPlugin :+
      RamlResourceTypeReference :+
      RamlParametrizedDeclarationVariablesRef :+
      RamlAbstractDefinition :+
      RamlTraitReference :+
      BaseUriParameterCompletionPlugin :+
      Raml08BaseUriParameterFacets :+
      RamlPayloadMediaTypeCompletionPlugin :+
      RamlNumberShapeFormatValues :+
      SecurityScopesCompletionPlugin :+
      SecuritySettingsFacetsCompletionPlugin :+
      ObjectExamplePropertiesCompletionPlugin :+
      ExampleStructure :+
      WebApiExtensionsPropertyCompletionPlugin :+
      RamlDeclarationsReferencesCompletionPlugin :+
      NodeShapeDiscriminatorProperty :+
      SecuredByCompletionPlugin :+
      Raml08SecuritySchemeStructureCompletionPlugin :+
      UnitDocumentationFacet :+
      DefaultVariablesAbstractDefinition :+
      OperationRequest :+
      Raml08KnownValueCompletionPlugin

  def init(): Unit =
    CompletionsPluginHandler.registerPlugins(all, RAML08Dialect().id)
}
