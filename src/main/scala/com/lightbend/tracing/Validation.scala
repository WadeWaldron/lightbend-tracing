package com.lightbend.tracing

import java.util.UUID
import akka.actor.{Props, ActorLogging, Actor}

import scala.util.Random

object Validation {
  case class Validate(orderId: UUID, amount: Float)
  case class Validated(orderId: UUID, amount: Float)

  def props(shouldFail: Boolean = false): Props = Props(new Validation(shouldFail))
}

class Validation(shouldFail: Boolean = false) extends Actor with ActorLogging {
  import Validation._

  override def receive: Receive = {
    case Validate(id, amount) =>
      if(shouldFail && Random.nextInt(10) <= 1)
        throw new IllegalStateException("Boom")

      log.info(s"Validated: $id, $amount")
      sender() ! Validated(id, amount)
  }
}
