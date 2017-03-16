package com.github.unknownnpc.debugtesttool

import akka.actor.{Props, _}
import com.github.unknownnpc.debugtesttool.actor.MainAppActor
import com.github.unknownnpc.debugtesttool.message.{MainAppActorStart, MainAppActorStop}
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object Main {

  private val log = LoggerFactory.getLogger(this.getClass)
  private implicit val actorSystem = ActorSystem("debug-test-tool")

  def main(args: Array[String]) {

    val mainActorRef = createMainAppActorRef()

    mainActorRef ! MainAppActorStart

    Runtime.getRuntime.addShutdownHook(
      new Thread() {
        override def run() {

          log.warn("Trying to terminate correctly")
          mainActorRef ! MainAppActorStop

          Await.ready(
            actorSystem.whenTerminated,
            atMost = 15 seconds
          )
          log.warn("Actors successfully stopped")
        }
      }
    )
  }

  private def createMainAppActorRef() = {
    actorSystem.actorOf(Props(new MainAppActor()), name = "main-app-actor")
  }

}
