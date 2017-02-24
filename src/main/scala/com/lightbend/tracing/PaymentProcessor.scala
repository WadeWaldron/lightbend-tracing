package com.lightbend.tracing

import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorLogging, Actor}

import scala.concurrent.duration._

object PaymentProcessor {
  case class CompletePayment(amount: Float)
  case class PaymentCompleted(amount: Float)

  def props(blocking: Boolean = false): Props = Props(new PaymentProcessor(blocking))
}

class PaymentProcessor(blocking: Boolean = false) extends Actor with ActorLogging {
  import PaymentProcessor._

  private val sleepTime = context.system.settings.config.getDuration("paymentProcessor.sleepTime", TimeUnit.MILLISECONDS).millis

  override def receive: Receive = {
    case CompletePayment(amount) =>
      if(blocking) {
        Thread.sleep(sleepTime.toMillis)
      }

      log.info(s"Completing Payment of $amount")
      sender() ! PaymentCompleted(amount)
  }
}
