package jester.service

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.RemoteAddress
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import jester.service.actors.SessionActor
import jester.service.routes.WebsocketFlow

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class GameService(implicit val actorSystem: ActorSystem,
                  implicit val actorMaterializer: ActorMaterializer,
                  implicit val executionContext: ExecutionContext)
  extends Directives {

  implicit val timeout: akka.util.Timeout = 10 seconds

  private val sessionWorker = actorSystem.actorOf(Props(new SessionActor()), "session-worker")

  private val websocketFlow = new WebsocketFlow(sessionWorker)

  private def defaultIpToUnknown(ip: RemoteAddress) = ip.toOption.map(_.getHostAddress).getOrElse("unknown")

  def route: Route = {

    get {
      (pathSingleSlash & parameter("sessionId") & extractClientIP) {
        (sessionId, ip) =>
          handleWebSocketMessages(websocketFlow.flow(sessionId, defaultIpToUnknown(ip)))
      }
    }
  }
}
