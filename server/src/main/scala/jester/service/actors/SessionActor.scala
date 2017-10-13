package jester.service.actors

import akka.actor.{Actor, ActorRef, PoisonPill}
import com.typesafe.scalalogging.LazyLogging
import jester.common.JokeId
import jester.service.events.{SessionEvent, SessionLogout, SessionStart}
import jester.service.messages.JesterMessages._

import scala.concurrent.ExecutionContext

/**
  * Created by matt on 30/04/17.
  */
class SessionActor(implicit val ec: ExecutionContext) extends Actor with LazyLogging {

  private val active = collection.mutable.LinkedHashMap[String, ActorRef]()

  private def getActive(sessionId: String): Option[ActorRef] = active.get(sessionId)

  private def addToActive(sessionId: String, userWorker: ActorRef): Unit =
    active += (sessionId -> userWorker)

  private def removeFromActive(sessionId: String): Unit = active -= sessionId

  private def cleanupDeadSession(sessionId: String): Unit =
    getActive(sessionId).foreach { oldUserWorker =>
      oldUserWorker ! PoisonPill
      removeFromActive(sessionId)
    }

  private def getStartingJokes(sessionId: String) = {
    // TODO get the next joke properly - and if seen this user (sessionId) before then
    StartingJokes(List(Joke(1, "content 1")))
  }

  private def recommendJoke(sessionId: String, jokeRatedId: JokeId) = {
    // TODO get the next joke properly
    Joke(99, "content 99")
  }

  private def handleRequest(request: JesterMessage, sessionId: String): Unit = {
    request match {
      case JokeRated(id, rating) =>
        // TODO save the joke rating for this user (don't wait for result)
        getActive(sessionId) foreach (_ ! recommendJoke(sessionId, id))

      case _: Logout => cleanupDeadSession(sessionId)

      case x =>
        logger.debug(s"unexpected message $x")
        getActive(sessionId) foreach (_ ! Invalid(s"unknown message"))
    }
  }

  override def receive: Receive = {

    case SessionStart(sessionId, ip, userWorker) =>
      logger.debug(s"session start $sessionId, $ip")
      cleanupDeadSession(sessionId)
      addToActive(sessionId, userWorker)
      userWorker ! getStartingJokes(sessionId)

    case SessionEvent(jesterMessage, sessionId) =>
      logger.debug(s"session start $sessionId, $jesterMessage")
      handleRequest(jesterMessage, sessionId)

    case SessionLogout(sessionId) =>
      logger.debug(s"session start $sessionId")
      removeFromActive(sessionId)

    case x => logger.debug(s"Got unexpected message: $x")
  }

}
