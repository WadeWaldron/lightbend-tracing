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
  case object ExtremeBlocking extends Mode
}

class PaymentProcessor(mode: Mode = PaymentProcessor.Normal) extends Actor with ActorLogging {
  import PaymentProcessor._

  private var counter = 1

  override def receive: Receive = {
    case CompletePayment(amount) =>
      log.info(s"Completing Payment of $amount")
      counter += 1

      if(mode == PaymentProcessor.ExtremeBlocking) {
        Thread.sleep(200)
      } else if(mode == PaymentProcessor.Blocking && counter % 1000 < 100) {
        Thread.sleep(200)
      }

      sender() ! PaymentCompleted(amount)
  }
}
