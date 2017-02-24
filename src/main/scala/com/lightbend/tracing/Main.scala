package com.lightbend.tracing

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.event.slf4j.Logger
import akka.routing.RoundRobinPool
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.duration._

abstract class BaseApp extends App {
  val log = Logger("BaseApp")

  for (arg <- args if arg.startsWith("-D")) {
    val keyVal = arg.replace("-D", "").split("=")
    System.setProperty(keyVal(0), keyVal(1))
  }

  val system = ActorSystem("Tracing")

  def shutdown(): Unit = {
    log.info("-------------")
    log.info("SHUTTING DOWN")
    log.info("-------------")

    system.terminate()
  }

  sys.addShutdownHook(shutdown())
}

object Producer extends BaseApp {
  import system.dispatcher

  val config = system.settings.config.getConfig("producer")

  val askTimeout = config.getDuration("askTimeout", TimeUnit.MILLISECONDS).millis

  val burstDelay = config.getDuration("burst.initialDelay", TimeUnit.MILLISECONDS).millis
  val burstFrequency = config.getDuration("burst.frequency", TimeUnit.MILLISECONDS).millis
  val burstSize = config.getInt("burst.size")

  val requestDelay = config.getDuration("request.initialDelay", TimeUnit.MILLISECONDS).millis
  val requestFrequency = config.getDuration("request.frequency", TimeUnit.MILLISECONDS).millis

  val mediator = DistributedPubSub(system).mediator

  implicit val timeout = Timeout(askTimeout)

  val regularTraffic = system.scheduler.schedule(requestDelay, requestFrequency) {
    val id = UUID.randomUUID()
    val request = system.actorOf(Request.props(mediator), id.toString)

    request ? Request.BeginRequest(id)
  }

  val bursts = system.scheduler.schedule(burstDelay, burstFrequency) {
    (1 to burstSize).foreach { _ =>
      val id = UUID.randomUUID()
      val request = system.actorOf(Request.props(mediator), id.toString)

      request ? Request.BeginRequest(id)
    }
  }

  override def shutdown(): Unit = {
    regularTraffic.cancel()
    bursts.cancel()
    super.shutdown()
  }
}

object Consumer extends BaseApp {
  val mediator = DistributedPubSub(system).mediator

  val validation = system.actorOf(Validation.props(), "validation")
  val orderManagement = system.actorOf(OrderManagement.props(), "order-management")
  val paymentProcessor = system.actorOf(PaymentProcessor.props(), "payment-processor")
  val shipping = system.actorOf(Shipping.props(), "shipping")

  mediator ! Subscribe(s"Validations", group = Some("Consumer"), validation)
  mediator ! Subscribe(s"Orders", group = Some("Consumer"), orderManagement)
  mediator ! Subscribe(s"Payments", group = Some("Consumer"), paymentProcessor)
  mediator ! Subscribe(s"Shipments", group = Some("Consumer"), shipping)
}

object Failing extends BaseApp {
  val mediator = DistributedPubSub(system).mediator

  val validation = system.actorOf(Validation.props(shouldFail = true), "validation")
  mediator ! Subscribe(s"Validations", group = Some("Consumer"), validation)
}

object Blocking extends BaseApp {
  val mediator = DistributedPubSub(system).mediator

  val paymentProcessor = system.actorOf(
    RoundRobinPool(50).props(PaymentProcessor.props(blocking = true)),
    "payment-processor"
  )

  mediator ! Subscribe(s"Payments", group = Some("Consumer"), paymentProcessor)
}

