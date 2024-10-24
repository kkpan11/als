package org.mulesoft.amfintegration.dialect.dialects.asyncapi26

import amf.aml.client.scala.model.domain.DocumentsModel
import amf.plugins.document.vocabularies.plugin.ReferenceStyles
import org.mulesoft.amfintegration.dialect.BaseDialect
import org.mulesoft.amfintegration.dialect.dialects.asyncapi20.bindings._
import org.mulesoft.amfintegration.dialect.dialects.asyncapi20.schema._
import org.mulesoft.amfintegration.dialect.dialects.asyncapi20.{AMLInfoObject, AsyncAPI20ApiKeySecurityObject, AsyncAPI20Auth20SecurityObject, AsyncAPI20FlowObject, AsyncAPI20HttpApiKeySecurityObject, AsyncAPI20HttpSecurityObject, AsyncAPI20penIdConnectUrl, AsyncApiVariableObject, CorrelationIdObjectNode, Oauth2FlowObject, OperationTraitsObjectNode, ParameterObjectNode, PayloadMessageObjectNode}
import org.mulesoft.amfintegration.dialect.dialects.asyncapi26.bindings.{AnypointMQChannelBindingObject, AnypointMQMessageBindingObject, ChannelBinding26ObjectNode, GooglePubSubChannelBinding10Object, GooglePubSubChannelBinding20Object, GooglePubSubMessageBinding10Object, GooglePubSubMessageBinding20Object, GooglePubSubMessageStoragePolicyObject, GooglePubSubSchemaDefinition10Object, GooglePubSubSchemaDefinition20Object, GooglePubSubSchemaSettingsObject, IBMMQChannelBindingObject, IBMMQChannelQueueObject, IBMMQChannelTopicObject, IBMMQMessageBindingObject, IBMMQServerBindingObject, MessageBinding26ObjectNode, OperationBinding26ObjectNode, PulsarChannelBindingObject, PulsarChannelRetentionObject, PulsarServerBindingObject, ServerBinding26ObjectNode, SolaceOperationBinding10Object, SolaceOperationBinding20Object, SolaceOperationBinding30Object, SolaceOperationBinding40Object, SolaceOperationDestination10Object, SolaceOperationDestination20Object, SolaceOperationDestination30Object, SolaceOperationDestination40Object, SolaceOperationQueue10Object, SolaceOperationQueue30Object, SolaceOperationTopicObject, SolaceServerBindingObject}
import org.mulesoft.amfintegration.dialect.dialects.oas.nodes._
import org.mulesoft.amfintegration.dialect.dialects.raml.raml10.Raml10TypesDialect

object AsyncApi26Dialect extends BaseDialect {
  private val _                        = Raml10TypesDialect().id // hack for ExampleNode.id
  override def DialectLocation: String = "file://vocabularies/dialects/asyncapi26.yaml"

  override val declares: Seq[DialectNode] = Seq(
    AsyncApi26ApiNode,
    AsyncApi26SecuritySettingsObject,
    AsyncApi26SecuritySchemeObject,
    AsyncAPI20ApiKeySecurityObject,
    AsyncAPI20HttpApiKeySecurityObject,
    AsyncAPI20HttpSecurityObject,
    AsyncAPI20Auth20SecurityObject,
    AsyncAPI20penIdConnectUrl,
    Oauth2FlowObject,
    AsyncAPI20FlowObject,
    Channel26Object,
    CorrelationIdObjectNode,
    Message26ObjectNode,
    ResponseMessage26ObjectNode,
    RequestMessage26ObjectNode,
    PayloadMessageObjectNode,
    MessageTraits26ObjectNode,
    Operation26Object,
    OperationTraitsObjectNode,
    ParameterObjectNode,
    ServerBindingObjectNode,
    BaseShapeAsync2Node,
    AnyShapeAsync2Node,
    ArrayShapeAsync2Node,
    NodeShapeAsync2Node,
    NumberShapeAsync2Node,
    StringShapeAsync2Node,
    ChannelBinding26ObjectNode,
    ServerBinding26ObjectNode,
    MessageBinding26ObjectNode,
    OperationBinding26ObjectNode,
    ServerBindingsObjectNode,
    OperationBindingsObjectNode,
    ChannelBindingsObjectNode,
    MessageBindingsObjectNode,
    AmqpChannelBindingObject,
    AmqpChannel010BindingObject,
    AmqpChannel020BindingObject,
    WsChannelBindingObject,
    LastWillMqttServerBindingObject,
    KafkaServerBindingObject,
    MqttServerBinding10ObjectNode,
    MqttServerBinding20ObjectNode,
    AmqpMessageBindingObjectNode,
    KafkaMessageBindingObjectNode,
    KafkaMessageBinding010ObjectNode,
    KafkaMessageBinding030ObjectNode,
    MqttMessageBinding10ObjectNode,
    MqttMessageBinding20ObjectNode,
    HttpMessageBinding20ObjectNode,
    HttpMessageBinding30ObjectNode,
    Amqp091OperationBindingObjectNode,
    Amqp091OperationBinding010ObjectNode,
    Amqp091OperationBinding030ObjectNode,
    KafkaOperationBindingObjectNode,
    HttpOperationBinding10ObjectNode,
    HttpOperationBinding20ObjectNode,
    MqttOperationBinding10ObjectNode,
    MqttOperationBinding20ObjectNode,
    QueueAmqpChannelBinding,
    QueueAmqpChannel010Binding,
    QueueAmqpChannel020Binding,
    KafkaChannelBinding,
    Kafka030ChannelBinding,
    Kafka040ChannelBinding,
    Kafka050ChannelBinding,
    KafkaTopicConfiguration040Object,
    KafkaTopicConfiguration050Object,
    RoutingKeyAmqpChannelBinding,
    RoutingKeyAmqpChannel010Binding,
    RoutingKeyAmqpChannel020Binding,
    StringShapeAsync2Node,
    AMLExternalDocumentationObject,
    AMLInfoObject,
    AMLContactObject,
    AMLLicenseObject,
    AMLTagObject,
    AsyncApi26ServerObject,
    IBMMQChannelBindingObject,
    IBMMQChannelQueueObject,
    IBMMQChannelTopicObject,
    GooglePubSubChannelBinding10Object,
    GooglePubSubChannelBinding20Object,
    GooglePubSubMessageStoragePolicyObject,
    GooglePubSubSchemaSettingsObject,
    AnypointMQChannelBindingObject,
    PulsarChannelBindingObject,
    PulsarChannelRetentionObject,
    IBMMQMessageBindingObject,
    GooglePubSubMessageBinding10Object,
    GooglePubSubMessageBinding20Object,
    GooglePubSubSchemaDefinition10Object,
    GooglePubSubSchemaDefinition20Object,
    AnypointMQMessageBindingObject,
    SolaceOperationBinding10Object,
    SolaceOperationBinding20Object,
    SolaceOperationBinding30Object,
    SolaceOperationBinding40Object,
    SolaceOperationDestination10Object,
    SolaceOperationDestination20Object,
    SolaceOperationDestination30Object,
    SolaceOperationDestination40Object,
    SolaceOperationQueue30Object,
    SolaceOperationQueue10Object,
    SolaceOperationTopicObject,
    IBMMQServerBindingObject,
    SolaceServerBindingObject,
    PulsarServerBindingObject
  )

  override def emptyDocument: DocumentsModel =
    DocumentsModel()
      .withId(DialectLocation + "#/documents")
      .withKeyProperty(true)
      .withReferenceStyle(ReferenceStyles.JSONSCHEMA)
      .withDeclarationsPath("components")

  override def encodes: DialectNode = AsyncApi26ApiNode

  override def declaredNodes: Map[String, DialectNode] = Map(
    "serverVariables"   -> AsyncApiVariableObject,
    "servers"           -> AsyncApi26ServerObject,
    "channels"          -> Channel26Object,
    "securitySchemes"   -> AsyncApi26SecuritySchemeObject,
    "messages"          -> Message26ObjectNode,
    "parameters"        -> ParameterObjectNode,
    "correlationIds"    -> CorrelationIdObjectNode,
    "operationTraits"   -> OperationTraitsObjectNode,
    "messageTraits"     -> MessageTraits26ObjectNode,
    "serverBindings"    -> ServerBindingsObjectNode,
    "channelBindings"   -> ChannelBindingsObjectNode,
    "operationBindings" -> OperationBindingsObjectNode,
    "messageBindings"   -> MessageBindingsObjectNode,
    "schemas"           -> BaseShapeAsync2Node
  )

  override protected val name: String    = "asyncapi"
  override protected val version: String = "2.6.0"
}
