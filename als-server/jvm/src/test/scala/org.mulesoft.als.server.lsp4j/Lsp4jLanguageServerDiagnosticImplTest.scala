package org.mulesoft.als.server.lsp4j

import java.util

import amf.core.unsafe.PlatformSecrets
import org.eclipse.lsp4j.ExecuteCommandParams
import org.mulesoft.als.server.protocol.LanguageServer
import org.mulesoft.als.server.modules.WorkspaceManagerFactoryBuilder
import org.mulesoft.als.server.{LanguageServerBaseTest, LanguageServerBuilder, MockDiagnosticClientNotifier}

import scala.concurrent.Future

class Lsp4jLanguageServerDiagnosticImplTest extends LanguageServerBaseTest with PlatformSecrets {

  val diagnosticsClient = new MockDiagnosticClientNotifier
  // TODO: check if a new validation should be sent from WorkspaceContentCollection when "onFocus" (when the BU is already parsed)
  test("Lsp4j LanguageServerImpl Command - Did Focus: Command should notify DidFocus") {
    def wrapJson(uri: String, version: String): String =
      s"""{"uri": "$uri", "version": "$version"}"""

    def executeCommandFocus(server: LanguageServerImpl)(file: String, version: Int): Future[Unit] = {
      val args: java.util.List[AnyRef] = new util.ArrayList[AnyRef]()
      args.add(wrapJson(file, version.toString))
      server.getWorkspaceService.executeCommand(new ExecuteCommandParams("didFocusChange", args))
//      MockDiagnosticClientNotifier.nextCall
      Future.successful(Unit)
    }

    withServer { s =>
      val server       = new LanguageServerImpl(s)
      val mainFilePath = s"file://api.raml"
      val libFilePath  = s"file://lib1.raml"

      val mainContent =
        """#%RAML 1.0
          |
          |title: test API
          |uses:
          |  lib1: lib1.raml
          |
          |/resource:
          |  post:
          |    responses:
          |      200:
          |        body:
          |          application/json:
          |            type: lib1.TestType
          |            example:
          |              {"a":"1"}
        """.stripMargin

      val libFileContent =
        """#%RAML 1.0 Library
          |
          |types:
          |  TestType:
          |    properties:
          |      b: string
        """.stripMargin

      /*
        open lib -> open main -> focus lib -> fix lib -> focus main
       */
      for {
        _      <- openFileNotification(s)(libFilePath, libFileContent)
        a      <- diagnosticsClient.nextCall
        _      <- openFileNotification(s)(mainFilePath, mainContent)
        b      <- diagnosticsClient.nextCall
        c      <- diagnosticsClient.nextCall // dependency of main
        _      <- executeCommandFocus(server)(libFilePath, 0)
        focus1 <- diagnosticsClient.nextCall
        _      <- changeNotification(s)(libFilePath, libFileContent.replace("b: string", "a: string"), 1)
        d      <- diagnosticsClient.nextCall
        _      <- executeCommandFocus(server)(mainFilePath, 0)
        focus2 <- diagnosticsClient.nextCall
        focus3 <- diagnosticsClient.nextCall
      } yield {
        server.shutdown()
        assert(
          a.diagnostics.isEmpty && a.uri == libFilePath &&
            b.diagnostics.length == 1 && b.uri == mainFilePath && // todo: search coinciding message between JS and JVM
            c.diagnostics.isEmpty && c.uri == libFilePath &&
            d.diagnostics.isEmpty && d.uri == libFilePath &&
            focus1 == a &&
            focus2.diagnostics.isEmpty && focus2.uri == mainFilePath &&
            focus3 == d
        )
      }
    }
  }

  test("diagnostics test - FullValidation") {
    def wrapJson(uri: String): String =
      s"""{"mainUri": "$uri"}"""

    withServer { s =>
      val mainFilePath = s"file://api.raml"
      val libFilePath  = s"file://lib1.raml"

      val mainContent =
        """#%RAML 1.0
          |
          |title: test API
          |uses:
          |  lib1: lib1.raml
          |
          |/resource:
          |  post:
          |    responses:
          |      200:
          |        body:
          |          application/json:
          |            type: lib1.TestType
          |            example:
          |              {"a":"1"}
        """.stripMargin

      val libFileContent =
        """#%RAML 1.0 Library
          |
          |types:
          |  TestType:
          |    properties:
          |      b: string
        """.stripMargin

      /*
        open lib -> open main -> focus lib -> fix lib -> focus main
       */
      for {
        _ <- {
          openFileNotification(s)(libFilePath, libFileContent)
          openFileNotification(s)(mainFilePath, mainContent)
        }
        v1 <- requestCleanDiagnostic(s)(mainFilePath)
        _  <- changeNotification(s)(libFilePath, libFileContent.replace("b: string", "a: string"), 1)
        v2 <- requestCleanDiagnostic(s)(mainFilePath)

      } yield {
        s.shutdown()

        diagnosticsClient.promises.clear()
        v1.size should be(2)
        v1.head.diagnostics.size should be(1)
        v1.last.diagnostics.size should be(0)
        v2.size should be(2) // fixed error
        v2.head.diagnostics.size should be(0)
      }
    }
  }

  override def buildServer(): LanguageServer = {
    val builder  = new WorkspaceManagerFactoryBuilder(diagnosticsClient, logger)
    val dm       = builder.diagnosticManager()
    val managers = builder.buildWorkspaceManagerFactory()

    new LanguageServerBuilder(managers.documentManager, managers.workspaceManager)
      .addInitializableModule(dm)
      .addRequestModule(managers.cleanDiagnosticManager)
      .build()
  }

  override def rootPath: String = ""
}