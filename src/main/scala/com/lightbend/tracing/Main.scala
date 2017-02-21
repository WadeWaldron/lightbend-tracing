package com.lightbend.tracing

import java.util.UUID

import akka.actor.{PoisonPill, Props, ActorSystem}
import akka.cluster.sharding.ShardRegion.MessageExtractor
import akka.cluster.sharding.{ClusterShardingSettings, ClusterSharding}
import akka.cluster.singleton.{ClusterSingletonManagerSettings, ClusterSingletonManager}
import akka.pattern.ask
import akka.util.Timeout
import org.slf4j.MDC

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

object Main extends App {

  val system = ActorSystem("Tracing")
  import system.dispatcher

  implicit val timeout = Timeout(10.seconds)

  val messageExtractor = new MessageExtractor {

    override def entityId(message: Any): String = message match {
      case Request.BeginRequest(requestId) => requestId.toString
    }

    override def shardId(message: Any): String = message match {
      case Request.BeginRequest(requestId) => (Math.abs(requestId.hashCode()) % 10).toString
    }

    override def entityMessage(message: Any): Any = message
  }

  system.actorOf(
    ClusterSingletonManager.props(
      singletonProps = Shipping.props(),
      terminationMessage = PoisonPill,
      settings = ClusterSingletonManagerSettings(system)),
    name = "shipping")


  val requests = ClusterSharding(system).start(
    "request",
    Request.props(),
    ClusterShardingSettings(system),
    messageExtractor
  )

  val futures = (1 to 10).map { _ =>
    requests ? Request.BeginRequest(UUID.randomUUID)
  }

  Future.sequence(futures).andThen {
    case result =>
      println(result)
      system.terminate()
  }

}
