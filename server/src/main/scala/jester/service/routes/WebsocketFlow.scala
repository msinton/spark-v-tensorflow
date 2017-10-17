package jester.service.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, OverflowStrategy}
import jester.service.events.{Event, SessionEvent, SessionLogout, SessionStart}
import jester.service.messages.JesterMessages._
import argonaut._
import Argonaut._
import com.typesafe.scalalogging.LazyLogging

class WebsocketFlow(sessionWorker: ActorRef) extends LazyLogging {

  import ArgonautShapeless._ // Do not remove - required for JSON parsing

  private val userActorSource = Source.actorRef[JesterMessage](bufferSize = 5, OverflowStrategy.fail)

  def flow(sessionId: String, ip: String): Flow[Message, Message, Any] =
    Flow.fromGraph(GraphDSL.create(userActorSource) { implicit builder =>
      userActor =>

        import GraphDSL.Implicits._

        val materialization = builder.materializedValue.map(actorRef =>
          SessionStart(sessionId, ip, actorRef))

        val merge = builder.add(Merge[Event](2))

        val messagesToEventFlow = builder.add(Flow[Message].collect {
          case TextMessage.Strict(msg) =>
            logger.debug(s"messagesToEventFlow  $msg")
            SessionEvent(msg.decodeOption[JesterMessage].getOrElse(Invalid("Not a recognised message")), sessionId)
        })

        val JesterMessagesToMessagesFlow = builder.add(Flow[JesterMessage].map(message => {
          logger.debug(s"JesterMessagesToMessagesFlow  $message ${message.asJson.nospaces}"); TextMessage(message.asJson.nospaces)}))

        val sessionActorSink = Sink.actorRef[Event](sessionWorker, SessionLogout(sessionId))

        materialization ~> merge ~> sessionActorSink
        messagesToEventFlow ~> merge

        userActor ~> JesterMessagesToMessagesFlow

        FlowShape(messagesToEventFlow.in, JesterMessagesToMessagesFlow.out)
    })
}