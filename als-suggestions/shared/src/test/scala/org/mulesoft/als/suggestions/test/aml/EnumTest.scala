package org.mulesoft.als.suggestions.test.aml

import amf.ProfileName
import org.mulesoft.typesystem.definition.system.{OasCommonMediaTypes, OasResponseCodes}

class EnumTest extends AMLSuggestionsTest {
  override def rootPath: String = "AML/enums"

  test("AllowMultiple Enum - Single Value") {
    withDialect("instances/testArraysInstance02.yaml",
                Set("First", "Second", "Third", "Fourth"),
                "dialects/testArraysDialect.yaml",
                ProfileName("Test Array 1.0"))
  }
  test("AllowMultiple Enum - Multiple Value") {
    withDialect("instances/testArraysInstance01.yaml",
                Set("Second", "Third", "Fourth"),
                "dialects/testArraysDialect.yaml",
                ProfileName("Test Array 1.0"))
  }
  test("AllowMultiple Enum - Single Value w/prefix") {
    withDialect("instances/testArraysInstance03.yaml",
                Set("First", "Fourth"),
                "dialects/testArraysDialect.yaml",
                ProfileName("Test Array 1.0"))
  }
  test("AllowMultiple Enum - Multiple Value w/prefix") {
    withDialect("instances/testArraysInstance04.yaml",
                Set("Fourth"),
                "dialects/testArraysDialect.yaml",
                ProfileName("Test Array 1.0"))
  }

  test("Known Values - Response Codes") {
    withDialect("instances/responseCodes.yaml",
                OasResponseCodes.all.map(r => s"$r:\n      ").toSet,
                "dialects/knownValuesDialect.yaml",
                ProfileName("KnownValues 1.0"))
  }

  test("Known Values - Media Types") {
    withDialect("instances/mediaTypes.yaml",
                OasCommonMediaTypes.all.map(r => s"$r:\n      ").toSet,
                "dialects/knownValuesDialect.yaml",
                ProfileName("KnownValues 1.0"))
  }

}