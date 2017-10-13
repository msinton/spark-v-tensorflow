package jester.service.events

import akka.actor.ActorRef
import jester.service.messages.JesterMessages.JesterMessage

case class SessionStart(sessionId: String, ip: String, actorRef: ActorRef) extends Event

case class SessionLogout(sessionId: String) extends Event

case class SessionEvent(JesterMessage: JesterMessage, sessionId: String) extends Event