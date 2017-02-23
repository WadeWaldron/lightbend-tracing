package com.lightbend.tracing

import java.util.UUID

import akka.actor.ActorSystem
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.duration._
import scala.util.Random

object Producer extends App {

  for (arg <- args if arg.startsWith("-D")) {
    val keyVal = arg.replace("-D", "").split("=")
    System.setProperty(keyVal(0), keyVal(1))
  }

  val system = ActorSystem("Tracing")
  import system.dispatcher

  val mediator = DistributedPubSub(system).mediator

  implicit val timeout = Timeout(10.seconds)

  val regularTraffic = system.scheduler.schedule(1.second, 100.millisecond) {
    val id = UUID.randomUUID()
    val request = system.actorOf(Request.props(mediator), id.toString)

    request ? Request.BeginRequest(id)
  }

  val bursts = system.scheduler.schedule(30.seconds, 30.seconds) {
    (1 to Random.nextInt(1000)).foreach { _ =>
      val id = UUID.randomUUID()
      val request = system.actorOf(Request.props(mediator), id.toString)

      request ? Request.BeginRequest(id)
    }
  }

  sys.addShutdownHook {
    println("-------------")
    println("SHUTTING DOWN")
    println("-------------")
    regularTraffic.cancel()
    bursts.cancel()
    system.terminate()
  }
}

object Consumer extends App {

  for (arg <- args if arg.startsWith("-D")) {
    val keyVal = arg.replace("-D", "").split("=")
    System.setProperty(keyVal(0), keyVal(1))
  }

  val system = ActorSystem("Tracing")

  val mediator = DistributedPubSub(system).mediator

  val validation = system.actorOf(Validation.props(), "validation")
  val orderManagement = system.actorOf(OrderManagement.props(), "order-management")
  val paymentProcessor = system.actorOf(PaymentProcessor.props(PaymentProcessor.Normal), "payment-processor")
  val shipping = system.actorOf(Shipping.props(), "shipping")

  mediator ! Subscribe(s"Validations", group = Some("Consumer"), validation)
  mediator ! Subscribe(s"Orders", group = Some("Consumer"), orderManagement)
  mediator ! Subscribe(s"Payments", group = Some("Consumer"), paymentProcessor)
  mediator ! Subscribe(s"Shipments", group = Some("Consumer"), shipping)

  sys.addShutdownHook {
    println("-------------")
    println("SHUTTING DOWN")
    println("-------------")
    system.terminate()
  }
}

object Failing extends App {

  for (arg <- args if arg.startsWith("-D")) {
    val keyVal = arg.replace("-D", "").split("=")
    System.setProperty(keyVal(0), keyVal(1))
  }

  val system = ActorSystem("Tracing")

  val mediator = DistributedPubSub(system).mediator

  val validation = system.actorOf(Validation.props(shouldFail = true), "validation")
  val orderManagement = system.actorOf(OrderManagement.props(), "order-management")
  val paymentProcessor = system.actorOf(PaymentProcessor.props(PaymentProcessor.Normal), "payment-processor")
  val shipping = system.actorOf(Shipping.props(), "shipping")

  mediator ! Subscribe(s"Validations", group = Some("Consumer"), validation)
  mediator ! Subscribe(s"Orders", group = Some("Consumer"), orderManagement)
  mediator ! Subscribe(s"Payments", group = Some("Consumer"), paymentProcessor)
  mediator ! Subscribe(s"Shipments", group = Some("Consumer"), shipping)

  sys.addShutdownHook {
    println("-------------")
    println("SHUTTING DOWN")
    println("-------------")
    system.terminate()
  }
}

object Blocking extends App {

  for (arg <- args if arg.startsWith("-D")) {
    val keyVal = arg.replace("-D", "").split("=")
    System.setProperty(keyVal(0), keyVal(1))
  }

  val system = ActorSystem("Tracing")

  val mediator = DistributedPubSub(system).mediator

  val validation = system.actorOf(Validation.props(), "validation")
  val orderManagement = system.actorOf(OrderManagement.props(), "order-management")
  val paymentProcessor = system.actorOf(PaymentProcessor.props(PaymentProcessor.Blocking), "payment-processor")
  val shipping = system.actorOf(Shipping.props(), "shipping")

  mediator ! Subscribe(s"Validations", group = Some("Consumer"), validation)
  mediator ! Subscribe(s"Orders", group = Some("Consumer"), orderManagement)
  mediator ! Subscribe(s"Payments", group = Some("Consumer"), paymentProcessor)
  mediator ! Subscribe(s"Shipments", group = Some("Consumer"), shipping)

  sys.addShutdownHook {
    println("-------------")
    println("SHUTTING DOWN")
    println("-------------")
    system.terminate()
  }
}

