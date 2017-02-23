package com.lightbend.tracing

import akka.actor.{Props, ActorLogging, Actor}
import com.lightbend.tracing.PaymentProcessor.Mode

object PaymentProcessor {
  case class CompletePayment(amount: Float)
  case class PaymentCompleted(amount: Float)

  def props(mode: Mode): Props = Props(new PaymentProcessor(mode))

  trait Mode
  case object Normal extends Mode
  case object Blocking extends Mode
}

class PaymentProcessor(mode: Mode = PaymentProcessor.Normal) extends Actor with ActorLogging {
  import PaymentProcessor._

  override def receive: Receive = {
    case CompletePayment(amount) =>
      if(mode == PaymentProcessor.Blocking) {
        Thread.sleep(200)
      }

      log.info(s"Completing Payment of $amount")
      sender() ! PaymentCompleted(amount)
  }
}
