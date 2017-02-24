package com.lightbend.tracing

import java.util.UUID

import akka.actor.{Props, ActorLogging, Actor}

import scala.util.Random

object OrderManagement {
  case object CreateOrder
  case class OrderCreated(orderId: UUID = UUID.randomUUID(), amount: Float = Random.nextInt(100))

  def props(): Props = Props(new OrderManagement)
}

class OrderManagement extends Actor with ActorLogging {
  import OrderManagement._

  override def receive: Receive = {
    case CreateOrder =>
      log.info("Creating Order")
      sender() ! OrderCreated()
  }
}
