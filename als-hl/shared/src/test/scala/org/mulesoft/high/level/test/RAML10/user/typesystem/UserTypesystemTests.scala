package org.mulesoft.high.level.test.RAML10.user.typesystem

import org.mulesoft.high.level.test.RAML10.RAML10TypesystemTest

class UserTypesystemTests extends RAML10TypesystemTest {

    test("Local type existence") {
        runTest("rootType/test001/api.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").head
            if (typeNode.localType.isDefined)
                succeed
            else
                fail("Local type is not defined for the node")
        })
    }

    test("Local type componentType") {
        runTest("rootType/test003.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").head
            if (typeNode.localType.get.array.get.componentType.get.nameId.get == "items")
                succeed
            else
                fail("Local type is not defined for the node")
        })
    }

    test("Local type options") {
        runTest("rootType/test004.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("arr")).get
            var unionSuperType = typeNode.localType.flatMap(_.superTypes.find(_.isUnion)).flatMap(_.union)
            if (unionSuperType.isDefined && unionSuperType.get.options.length == 2)
                succeed
            else
                fail("Local type is not defined for the node")
        })
    }

    test("Local type superTypes") {
        runTest("rootType/test005.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("arr")).get
            if (typeNode.localType.get.superTypes.length == 2)
                succeed
            else
                fail("Local type is not defined for the node")
        })
    }

    test("Local type subTypes") {
        runTest("rootType/test006.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("CustomDate")).get
            if (typeNode.localType.get.subTypes.length == 1)
                succeed
            else
                fail("Local type is not defined for the node")
        })
    }

    test("Local type allSubTypes") {
        runTest("rootType/test007.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("CustomDate")).get
            if (typeNode.localType.get.allSubTypes.length == 2)
                succeed
            else
                fail("Local type is not defined for the node")
        })
    }

    test("Local type allSuperTypes") {
        runTest("rootType/test008.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("abc")).get
            var actualValue = typeNode.localType.get.allSuperTypes.length
            var expectedValue = 3
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type properties") {
        runTest("rootType/test009.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("abc")).get
            if (typeNode.localType.get.properties.length == 2)
                succeed
            else
                fail("Local type is not defined for the node")
        })
    }

    test("Local type facet") {
        runTest("rootType/test010.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("CustomDate")).get
            if (typeNode.localType.get.facet("noHolidays").get.nameId.get == "noHolidays")
                succeed
            else
                fail("Local type is not defined for the node")
        })
    }

    test("Local type allProperties") {
        runTest("rootType/test011.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("arr")).get
            if (typeNode.localType.get.allProperties.length == 2)
                succeed
            else
                fail("Local type is not defined for the node")
        })
    }

    test("Local type allFacets") {
        runTest("rootType/test012.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("PossibleMeetingDate")).get
            var actualValue = typeNode.localType.get.allFacets.length
            var expectedValue = 3
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type facets") {
        runTest("rootType/test013.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("PossibleMeetingDate")).get
            var actualValue = typeNode.localType.get.facets.length
            var expectedValue = 1
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type isAnnotationType") {
        runTest("rootType/test014.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("PossibleMeetingDate")).get
            var actualValue = typeNode.localType.get.isAnnotationType
            var expectedValue = false
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type isBuiltIn") {
        runTest("rootType/test015.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("PossibleMeetingDate")).get
            var actualValue = typeNode.localType.get.isBuiltIn
            var expectedValue = false
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type requiredProperties") {
        runTest("rootType/test016.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("abc")).get
            var actualValue = typeNode.localType.get.requiredProperties.length
            var expectedValue = 2
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type getFixedFacets") {
        runTest("rootType/test017.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("PossibleMeetingDate")).get
            var actualValue = typeNode.localType.get.getFixedFacets.size
            var expectedValue = 1
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type fixedFacets") {
        runTest("rootType/test018.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("PossibleMeetingDate")).get
            var actualValue = typeNode.localType.get.fixedFacets.size
            var expectedValue = 1
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type allFixedFacets") {
        runTest("rootType/test019.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("PossibleMeetingDate")).get
            var actualValue = typeNode.localType.get.allFixedFacets.size
            var expectedValue = 1
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type fixedBuiltInFacets") {
        runTest("rootType/test020.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("abc")).get
            var actualValue = typeNode.localType.get.fixedBuiltInFacets.size
            var expectedValue = 3
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type allFixedBuiltInFacets") {
        runTest("rootType/test021.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("abc")).get
            var actualValue = typeNode.localType.get.allFixedBuiltInFacets.size
            var expectedValue = 3
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type isTopLevel") {
        runTest("rootType/test022.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("abc")).get
            var actualValue = typeNode.localType.get.isTopLevel
            var expectedValue = true
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type isUserDefined") {
        runTest("rootType/test023.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("abc")).get
            var actualValue = typeNode.localType.get.isUserDefined
            var expectedValue = true
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type isValueType") {
        runTest("rootType/test025.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("abc")).get
            var actualValue = typeNode.localType.get.isValueType
            var expectedValue = false
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type isArray") {
        runTest("rootType/test027.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("bb")).get
            var actualValue = typeNode.localType.get.isArray
            var expectedValue = true
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type isObject") {
        runTest("rootType/test028.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("bb")).get
            var actualValue = typeNode.localType.get.isObject
            var expectedValue = false
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type array") {
        runTest("rootType/test030.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("bb")).get
            var actualValue = typeNode.localType.get.array.get.nameId.get
            var expectedValue = "bb"
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type hasStructure") {
        runTest("rootType/test037.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("abc")).get
            var actualValue = typeNode.localType.get.hasStructure
            var expectedValue = true
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type property") {
        runTest("rootType/test044.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("abc")).get
            var actualValue = typeNode.localType.get.property("aa").get.nameId.get
            var expectedValue = "aa"
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type printDetails") {
        runTest("rootType/test045.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("prmn")).get
            var actualValue = typeNode.localType.get.printDetails
            var expectedValue = "prmn[org.mulesoft.typesystem.nominal_types.ValueType]\n  Super types:\n    string[org.mulesoft.typesystem.nominal_types.ValueType]\n      Super types:\n        scalar[org.mulesoft.typesystem.nominal_types.ValueType]\n          Super types:\n            any[org.mulesoft.typesystem.nominal_types.StructuredType]"
            if (actualValue.trim == expectedValue.trim)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Local type kind") {
        runTest("rootType/test049.raml", project => {

            var typeNode = project.rootASTUnit.rootNode.elements("types").find(t=> t.attribute("name").get.value.contains("abc")).get
            var actualValue = typeNode.localType.get.kind.length
            var expectedValue = 1
            if (expectedValue == actualValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }
}