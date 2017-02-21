package com.lightbend.tracing

import java.util.UUID
import akka.actor.{Props, ActorLogging, Actor}

object Validation {
  case class Validate(orderId: UUID, amount: Float)
  case class Validated(orderId: UUID, amount: Float)

  def props(): Props = Props(new Validation)
}

class Validation extends Actor with ActorLogging {
  import Validation._

  override def receive: Receive = {
    case Validate(id, amount) =>
      log.info(s"Validated: $id, $amount")
      sender() ! Validated(id, amount)
  }
}
