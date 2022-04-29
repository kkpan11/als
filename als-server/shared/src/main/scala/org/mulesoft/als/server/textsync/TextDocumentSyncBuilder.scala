package org.mulesoft.als.server.textsync

import org.mulesoft.als.logger.Logger
import org.mulesoft.als.server.modules.ast.TextListener
import org.mulesoft.als.server.protocol.textsync.AlsTextDocumentSyncConsumer

trait TextDocumentSyncBuilder {
  def build(
      container: TextDocumentContainer,
      dependencies: List[TextListener],
      logger: Logger
  ): AlsTextDocumentSyncConsumer
}

object DefaultTextDocumentSyncBuilder extends TextDocumentSyncBuilder {
  override def build(
      container: TextDocumentContainer,
      dependencies: List[TextListener],
      logger: Logger
  ): AlsTextDocumentSyncConsumer =
    new TextDocumentManager(container, dependencies, logger)
}
