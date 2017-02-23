package com.lightbend.tracing

import akka.actor.{Props, ActorLogging, Actor}
import com.lightbend.tracing.PaymentProcessor.Mode

object PaymentProcessor {
  case class CompletePayment(amount: Float)
  case class PaymentCompleted(amount: Float)

  def props(blocking: Boolean = false): Props = Props(new PaymentProcessor(blocking))

  trait Mode
  case object Normal extends Mode
  case object Blocking extends Mode
}

class PaymentProcessor(blocking: Boolean = false) extends Actor with ActorLogging {
  import PaymentProcessor._

  override def receive: Receive = {
    case CompletePayment(amount) =>
      if(blocking) {
        Thread.sleep(500)
      }

      log.info(s"Completing Payment of $amount")
      sender() ! PaymentCompleted(amount)
  }
}
