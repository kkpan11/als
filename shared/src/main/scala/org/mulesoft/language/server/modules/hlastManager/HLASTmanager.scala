package org.mulesoft.language.server.modules.hlastManager


import org.mulesoft.language.common.dtoTypes.{IRange, IValidationIssue, IValidationReport}
import org.mulesoft.language.server.core.{AbstractServerModule, IServerModule}
import org.mulesoft.language.server.server.modules.astManager.{IASTListener, IASTManagerModule, ParserHelper}
import org.mulesoft.language.server.server.modules.commonInterfaces.{IEditorTextBuffer, IPoint}
import org.mulesoft.language.server.server.modules.editorManager.IEditorManagerModule

import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import org.mulesoft.high.level.Core
import org.mulesoft.high.level.interfaces.IProject
import amf.core.model.document.BaseUnit

import scala.collection.mutable


class HLASTManager extends AbstractServerModule with IHLASTManagerModule {



  val moduleDependencies: Array[String] = Array(
    IEditorManagerModule.moduleId, IASTManagerModule.moduleId)

  var astListeners: Buffer[IHLASTListener] = ArrayBuffer()

  var currentASTs: mutable.Map[String, IProject] = mutable.HashMap()

  val onNewASTAvailableListener: IASTListener = new IASTListener {

    override def apply(uri: String, version: Int, ast: BaseUnit): Unit = {
      HLASTManager.this.newASTAvailable(uri, version, ast)
    }
  }

  protected def getEditorManager: IEditorManagerModule = {

    this.getDependencyById(IEditorManagerModule.moduleId).get
  }

  protected def getASTManager: IASTManagerModule = {

    this.getDependencyById(IASTManagerModule.moduleId).get
  }

  override def launch(): Try[IServerModule] = {

    val superLaunch = super.launch()

    if (superLaunch.isSuccess) {

      this.getASTManager.onNewASTAvailable(this.onNewASTAvailableListener)

      Success(this)
    } else {

      superLaunch
    }
  }


  override def stop(): Unit = {

    super.stop()

    this.getASTManager.onNewASTAvailable(this.onNewASTAvailableListener, true)
  }

  def onNewASTAvailable(listener: IHLASTListener, unsubscribe: Boolean = false): Unit = {

    this.addListener(this.astListeners, listener, unsubscribe)
  }

  def newASTAvailable(uri: String, version: Int, ast: BaseUnit): Unit = {

    this.connection.debug("Got new AST:\n" + ast.toString,
      "HLASTManager", "newASTAvailable")

    val projectFuture = this.hlFromAST(ast);

    projectFuture.map(project => {

      this.notifyASTChanged(uri, version, project)
    })
  }

  def notifyASTChanged(uri: String, version: Int, project: IProject) = {

    this.connection.debug("Got new AST parser results, notifying the listeners",
      "HLASTManager", "notifyASTChanged")

    this.astListeners.foreach { listener =>

      listener.apply(uri, version, project.rootASTUnit.rootNode)
    }

  }

  def hlFromAST(ast: BaseUnit): Future[IProject] = {

    Core.buildModel(ast, this.platform)
  }

  def forceGetCurrentAST(uri: String): Future[IProject] = {

    // TODO use runnable

    val current = this.currentASTs.get(uri)

    if (current.isDefined) {

      Future.successful(current.get)
    } else {

      this.getASTManager.forceGetCurrentAST(uri).flatMap(ast=>{
        this.hlFromAST(ast)
      })
    }
  }

  def addListener[T](memberListeners: Buffer[T], listener: T, unsubscribe: Boolean = false): Unit = {

    if (unsubscribe) {

      val index = memberListeners.indexOf(listener)
      if (index != -1) {
        memberListeners.remove(index)
      }

    }
    else {

      memberListeners += listener

    }

  }
}
object HLASTManager {

  /**
    * Module ID
    */
  val moduleId: String = "HL_AST_MANAGER"
}