package org.mulesoft.als.server

import org.mulesoft.als.server.workspace.{WorkspaceManager, WorkspaceRootHandler}
import org.mulesoft.lsp.configuration.{InitializeParams, InitializeResult}
import org.mulesoft.lsp.feature.{RequestHandler, RequestType}
import org.mulesoft.lsp.server.LanguageServer
import org.mulesoft.lsp.textsync.TextDocumentSyncConsumer
import org.mulesoft.lsp.workspace.WorkspaceService

import scala.concurrent.Future

class LanguageServerImpl(val textDocumentSyncConsumer: TextDocumentSyncConsumer,
                         val workspaceRootHandler: WorkspaceRootHandler,
                         private val languageServerInitializer: LanguageServerInitializer,
                         private val requestHandlerMap: RequestMap)
    extends LanguageServer {

  override def initialize(params: InitializeParams): Future[InitializeResult] = {
    params.rootUri.orElse(params.rootPath).foreach(workspaceRootHandler.addRootDir)
    languageServerInitializer.initialize(params)
  }

  override def initialized(): Unit = {}

  override def shutdown(): Unit = {}

  override def exit(): Unit = {}

  override def resolveHandler[P, R](requestType: RequestType[P, R]): Option[RequestHandler[P, R]] =
    requestHandlerMap(requestType)

  lazy val workspaceManager = new WorkspaceManager(workspaceRootHandler)

  override def workspaceService: WorkspaceService = workspaceManager
}
