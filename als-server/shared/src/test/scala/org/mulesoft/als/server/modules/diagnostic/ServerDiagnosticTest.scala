package org.mulesoft.als.server.modules.diagnostic

import amf.core.remote.Platform
import amf.internal.environment.Environment
import org.mulesoft.als.common.DirectoryResolver
import org.mulesoft.als.server.client.ClientNotifier
import org.mulesoft.als.server.modules.ast.AstManager
import org.mulesoft.als.server.textsync.TextDocumentManager
import org.mulesoft.als.server.{LanguageServerBaseTest, LanguageServerBuilder}
import org.mulesoft.lsp.feature.diagnostic.PublishDiagnosticsParams
import org.mulesoft.lsp.server.LanguageServer

import scala.concurrent.{ExecutionContext, Future, Promise}

class ServerDiagnosticTest extends LanguageServerBaseTest {

  override implicit val executionContext = ExecutionContext.Implicits.global

  override def rootPath: String = ""

  private object MockClientNotifier extends ClientNotifier {
    var promise: Option[Promise[PublishDiagnosticsParams]] = None

    def nextCall: Future[PublishDiagnosticsParams] = {
      if (promise.isEmpty)
        promise = Some(Promise[PublishDiagnosticsParams]())
      promise.get.future
    }

    override def notifyDiagnostic(params: PublishDiagnosticsParams): Unit = {
      promise.foreach(_.success(params))
      promise = None
    }
  }

  def openFileNotification(server: LanguageServer)(file: String, content: String): Future[PublishDiagnosticsParams] = {
    openFile(server)(file, content)
    MockClientNotifier.nextCall
  }

  def focusNotification(server: LanguageServer)(file: String, version: Int): Future[PublishDiagnosticsParams] = {
    onFocus(server)(file, version)
    MockClientNotifier.nextCall
  }

  def changeNotification(
      server: LanguageServer)(file: String, content: String, version: Int): Future[PublishDiagnosticsParams] = {
    changeFile(server)(file, content, version)
    MockClientNotifier.nextCall
  }

  override def addModules(documentManager: TextDocumentManager,
                          platform: Platform,
                          directoryResolver: DirectoryResolver,
                          baseEnvironment: Environment,
                          builder: LanguageServerBuilder): LanguageServerBuilder = {

    val astManager        = new AstManager(documentManager, baseEnvironment, platform, logger)
    val diagnosticManager = new DiagnosticManager(documentManager, astManager, MockClientNotifier, platform, logger)

    builder
      .addInitializable(astManager)
      .addInitializableModule(diagnosticManager)
  }

  test("diagnostics test 001 - onFocus") {
    withServer { server =>
      val mainFilePath = s"api.raml"
      val libFilePath  = s"lib1.raml"

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
        a <- openFileNotification(server)(libFilePath, libFileContent)
        b <- openFileNotification(server)(mainFilePath, mainContent)
        c <- focusNotification(server)(libFilePath, 0)
        d <- changeNotification(server)(libFilePath, libFileContent.replace("b: string", "a: string"), 1)
        e <- focusNotification(server)(mainFilePath, 0)
      } yield {
        server.shutdown()
        assert(
          a.diagnostics.isEmpty && a.uri == libFilePath &&
            b.diagnostics.length == 1 && b.uri == mainFilePath && // todo: search coinciding message between JS and JVM
            c.diagnostics.isEmpty && c.uri == libFilePath &&
            d.diagnostics.isEmpty && d.uri == libFilePath &&
            e.diagnostics.isEmpty && e.uri == mainFilePath)
      }
    }
  }
}
