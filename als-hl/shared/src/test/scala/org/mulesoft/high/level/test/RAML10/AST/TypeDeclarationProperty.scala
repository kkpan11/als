package org.mulesoft.high.level.test.RAML10.AST

import org.mulesoft.high.level.test.RAML10.RAML10ASTTest

class TypeDeclarationProperty extends RAML10ASTTest {

  test("TypeDeclaration name") {
    runTest("ASTTests/TypeDeclaration/typeDeclarationProperty.raml", project => {

      var expectedValue = "airplane"
      project.rootASTUnit.rootNode.elements("types").head.elements("properties").head.attribute("name") match {
        case Some(a) => a match {
          case expectedValue => succeed
          case _ => fail(s"Expected value: $expectedValue, actual: ${a}")
        }
        case _ => fail("'name' attribute not found")
      }
    })
  }

  test("TypeDeclaration displayName"){
    runTest( "ASTTests/TypeDeclaration/typeDeclarationProperty.raml", project => {

      var expectedValue = "plane"
      project.rootASTUnit.rootNode.elements("types").head.elements("properties").head.attribute("displayName") match {
        case Some(a) => a.value match {
          case Some(expectedValue) => succeed
          case _ => fail(s"Expected value: $expectedValue, actual: ${a.value}")
        }
        case _ => fail("'displayName' attribute not found")
      }
    })
  }

  test("TypeDeclaration description"){
    runTest( "ASTTests/TypeDeclaration/typeDeclarationProperty.raml", project => {

      var expectedValue = "none"
      project.rootASTUnit.rootNode.elements("types").head.elements("properties").head.attribute("description") match {
        case Some(a) => a.value match {
          case Some(expectedValue) => succeed
          case _ => fail(s"Expected value: $expectedValue, actual: ${a.value}")
        }
        case _ => fail("'displayName' attribute not found")
      }
    })
  }

  test("TypeDeclaration facets"){
    runTest( "ASTTests/TypeDeclaration/typeDeclarationProperty.raml", project => {

      var expectedValue = 1
      var length = project.rootASTUnit.rootNode.elements("types").head.elements("properties").head.elements("facets").length
      if (length == expectedValue)
        succeed
      else
        fail(s"Expected value: $expectedValue, actual: ${length}")
    })
  }

  test("TypeDeclaration examples"){
    runTest( "ASTTests/TypeDeclaration/typeDeclarationProperty.raml", project => {

      var expectedValue = 2
      var length = project.rootASTUnit.rootNode.elements("types").head.elements("properties").head.elements("examples").length
      if (length == expectedValue)
        succeed
      else
        fail(s"Expected value: $expectedValue, actual: ${length}")
    })
  }

  test("TypeDeclaration example"){
    runTest( "ASTTests/TypeDeclaration/typeDeclarationProperty.raml", project => {

      var expectedValue = 1
      var length = project.rootASTUnit.rootNode.elements("types").head.elements("properties").head.elements("example").length
      if (length == expectedValue)
        succeed
      else
        fail(s"Expected value: $expectedValue, actual: ${length}")
    })
  }

  test("TypeDeclaration xml") {
    runTest("ASTTests/TypeDeclaration/typeDeclarationProperty.raml", project => {

      var expectedValue = 1
      var length = project.rootASTUnit.rootNode.elements("types").head.elements("properties").head.elements("xml").length
      if (length == expectedValue)
        succeed
      else
        fail(s"Expected value: $expectedValue, actual: ${length}")
    })
  }

  test("TypeDeclaration required"){
    runTest( "ASTTests/TypeDeclaration/typeDeclarationProperty.raml", project => {

      var expectedValue = "true"
      project.rootASTUnit.rootNode.elements("types").head.elements("properties").head.attribute("required") match {
        case Some(a) => a.value match {
          case Some(expectedValue) => succeed
          case _ => fail(s"Expected value: $expectedValue, actual: ${a.value}")
        }
        case _ => fail("'displayName' attribute not found")
      }
    })
  }
}