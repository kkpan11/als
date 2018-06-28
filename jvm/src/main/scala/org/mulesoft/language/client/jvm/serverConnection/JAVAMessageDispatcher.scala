package org.mulesoft.language.client.jvm.serverConnection

import org.mulesoft.language.client.jvm.dtoTypes.ProtocolMessagePayload
import org.mulesoft.language.common.logger.ILogger
import org.mulesoft.language.entryPoints.common.{MessageDispatcher, ProtocolMessage, ProtocolSeqMessage};

trait JAVAMessageDispatcher extends MessageDispatcher[ProtocolMessagePayload, JAVAMessageType]  with ILogger {
	def internalSendJSONMessage(message: Any) {
	
	}
	
	def handleJSONMessageRecieved(message: Any) {
	
	}
	
	def internalSendMessage(message: ProtocolMessage[ProtocolMessagePayload]) {
	
	}
	
	def internalSendSeqMessage(message: ProtocolSeqMessage[ProtocolMessagePayload]) {
	
	}
}

