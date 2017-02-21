package com.lightbend.tracing

import java.util.UUID

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import akka.cluster.sharding.ShardRegion.MessageExtractor
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.cluster.singleton.{ClusterSingletonProxySettings, ClusterSingletonProxy}
import akka.util.Timeout
import org.slf4j.MDC

import scala.concurrent.Await
import scala.concurrent.duration._

object Request {
  case class BeginRequest(requestId: UUID)
  case class RequestCompleted(requestId: UUID)

  def props(): Props = Props(new Request())
}

class Request() extends Actor with ActorLogging {
  import Request._

  val requestId = UUID.fromString(self.path.name)

  private val orderManagement = createOrderManagement()
  private val paymentProcessor = createPaymentProcessor()
  private val shipping = createShipping()

  protected def createOrderManagement() = {
    context.actorOf(OrderManagement.props(), "order-management")
  }

  protected def createPaymentProcessor() = {
    context.actorOf(PaymentProcessor.props(), "payment-processor")
  }

  protected def createShipping() = {
//    context.actorOf(Shipping.props(), "shipping")
//
//    implicit val timeout = Timeout(5.seconds)
//
//    Await.result(context.actorSelection(self.path.toString.replace("akka://Tracing", "akka.tcp://Tracing@127.0.0.1:2551") + "/shipping").resolveOne(), 5.seconds)

//    context.actorOf(
//      ClusterSingletonProxy.props(
//        singletonManagerPath = "/user/shipping",
//        settings = ClusterSingletonProxySettings(context.system)),
//      name = "shipping")



    val messageExtractor = new MessageExtractor {
      override def entityId(message: Any): String = message match {
        case Shipping.ShipOrder(orderId) => orderId.toString
      }
      override def shardId(message: Any): String = message match {
        case Shipping.ShipOrder(orderId) => (Math.abs(orderId.hashCode()) % 10).toString
      }
      override def entityMessage(message: Any): Any = message
    }

    ClusterSharding(context.system).start(
      "shipping",
      Shipping.props(),
      ClusterShardingSettings(context.system),
      messageExtractor
    )
  }

  override def receive: Receive = {
    case BeginRequest(_) =>
      MDC.put("requestId", requestId.toString)
      log.info("Begin Request")
      orderManagement ! OrderManagement.CreateOrder
      context.become(creatingOrder(sender()))
  }

  private def creatingOrder(origin: ActorRef): Receive = {
    case OrderManagement.OrderCreated(orderId, amount) =>
      paymentProcessor ! PaymentProcessor.CompletePayment(amount)
      context.become(requestingPayment(origin, orderId))
  }

  private def requestingPayment(origin: ActorRef, orderId: UUID): Receive = {
    case PaymentProcessor.PaymentCompleted(amount) =>
      shipping ! Shipping.ShipOrder(orderId)
      context.become(shippingOrder(origin))
  }

  private def shippingOrder(origin: ActorRef): Receive = {
    case Shipping.OrderShipped(orderId) =>
      origin ! RequestCompleted(requestId)
      context.become(receive)
  }
}
