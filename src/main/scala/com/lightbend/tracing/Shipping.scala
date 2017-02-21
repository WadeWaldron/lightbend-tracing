package com.lightbend.tracing

import java.util.UUID

import akka.actor.{Props, ActorLogging, Actor}

object Shipping {
  case class ShipOrder(orderId: UUID)
  case class OrderShipped(orderId: UUID)

  def props(): Props = Props(new Shipping)

}

class Shipping extends Actor with ActorLogging {
  import Shipping._

  override def receive: Receive = {
    case ShipOrder(orderId) =>
      log.info(s"Shipping Order: $orderId")
      sender() ! OrderShipped(orderId)
  }
}
