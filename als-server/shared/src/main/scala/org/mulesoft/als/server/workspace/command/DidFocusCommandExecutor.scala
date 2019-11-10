package org.mulesoft.als.server.workspace.command

import org.mulesoft.als.server.logger.Logger
import org.mulesoft.als.server.modules.ast.FOCUS_FILE
import org.mulesoft.als.server.workspace.WorkspaceContentCollection
import org.mulesoft.lsp.textsync.DidFocusParams
import org.yaml.model.YMap
import amf.core.parser._

class DidFocusCommandExecutor(val logger: Logger, wsc: WorkspaceContentCollection)
    extends CommandExecutor[DidFocusParams] {
  override protected def buildParamFromMap(m: YMap): Option[DidFocusParams] = {
    val version: Int = m.key("version").flatMap(e => e.value.toOption[Int]).getOrElse(1)
    m.key("uri").map(_.value.value.toString) match {
      case Some(uri) => Some(DidFocusParams(uri, version))
      case _         => None
    }
  }

  override protected def runCommand(param: DidFocusParams): Unit = {
    wsc.getWorkspace(param.uri).changedFile(param.uri, FOCUS_FILE)
  }
}
