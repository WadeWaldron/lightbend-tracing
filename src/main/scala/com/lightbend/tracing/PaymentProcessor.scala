package com.lightbend.tracing

import akka.actor.{Props, ActorLogging, Actor}

object PaymentProcessor {
  case class CompletePayment(amount: Float)
  case class PaymentCompleted(amount: Float)

  def props(): Props = Props(new PaymentProcessor)
}

class PaymentProcessor extends Actor with ActorLogging {
  import PaymentProcessor._

  override def receive: Receive = {
    case CompletePayment(amount) =>
      log.info(s"Completing Payment of $amount")
      sender() ! PaymentCompleted(amount)
  }
}
