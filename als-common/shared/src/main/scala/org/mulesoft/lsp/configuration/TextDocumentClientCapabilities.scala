package org.mulesoft.lsp.configuration

import org.mulesoft.lsp.feature.codeactions.CodeActionCapabilities
import org.mulesoft.lsp.feature.completion.CompletionClientCapabilities
import org.mulesoft.lsp.feature.definition.DefinitionClientCapabilities
import org.mulesoft.lsp.feature.diagnostic.DiagnosticClientCapabilities
import org.mulesoft.lsp.feature.documentsymbol.DocumentSymbolClientCapabilities
import org.mulesoft.lsp.feature.reference.ReferenceClientCapabilities
import org.mulesoft.lsp.feature.rename.RenameClientCapabilities
import org.mulesoft.lsp.textsync.SynchronizationClientCapabilities

/**
  * Text document specific client capabilities.
  */
case class TextDocumentClientCapabilities(synchronization: Option[SynchronizationClientCapabilities] = None,
                                          publishDiagnostics: Option[DiagnosticClientCapabilities] = None,
                                          completion: Option[CompletionClientCapabilities] = None,
                                          references: Option[ReferenceClientCapabilities] = None,
                                          documentSymbol: Option[DocumentSymbolClientCapabilities] = None,
                                          definition: Option[DefinitionClientCapabilities] = None,
                                          rename: Option[RenameClientCapabilities] = None,
                                          codeActionCapabilities: Option[CodeActionCapabilities] = None,
                                         )