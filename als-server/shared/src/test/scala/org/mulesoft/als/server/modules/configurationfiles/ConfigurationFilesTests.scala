package org.mulesoft.als.server.modules.configurationfiles

import amf.core.unsafe.PlatformSecrets
import org.mulesoft.als.server.workspace.extract.WorkspaceRootHandler
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class ConfigurationFilesTests extends AsyncFlatSpec with Matchers with PlatformSecrets {

  override val executionContext: ExecutionContext = global

  behavior of "ProjectManager"
  private val okRoot = "file://als-server/shared/src/test/resources/configuration-files"

  it should "add a mainApi given a directory with exchange.json" in {
    val manager = new WorkspaceRootHandler(platform)
    manager.extractConfiguration(s"$okRoot/").map { conf =>
      conf.isDefined should be(true)
      conf.get.mainFile should be("api.raml")
    }
  }

  it should "Directory without exchange.json should not add any mainFile" in {
    val manager = new WorkspaceRootHandler(platform)
    manager.extractConfiguration(s"file://als-server/shared/src/test/resources/").map {
      _.isEmpty should be(true)
    }
  }
}