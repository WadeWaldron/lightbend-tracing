package com.lightbend.tracing

import java.util.UUID

import akka.actor.{PoisonPill, Props, ActorSystem}
import akka.cluster.sharding.ShardRegion.MessageExtractor
import akka.cluster.sharding.{ClusterShardingSettings, ClusterSharding}
import akka.cluster.singleton.{ClusterSingletonManagerSettings, ClusterSingletonManager}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

object Main extends App {

  val system = ActorSystem("Tracing")
  import system.dispatcher

  implicit val timeout = Timeout(10.seconds)

  val singleton = system.actorOf(
    ClusterSingletonManager.props(
      OrderManagement.props(),
      PoisonPill,
      ClusterSingletonManagerSettings(system)
    ),
    "order-management"
  )

  val futures = (1 to 10).map { _ =>
    val id = UUID.randomUUID()

    val request = system.actorOf(Request.props(), id.toString)

    request ? Request.BeginRequest(id)
  }

  Future.sequence(futures).andThen {
    case result =>
      println(result)
      system.terminate()
  }

}
