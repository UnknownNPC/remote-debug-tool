package com.github.unknownnpc.remotedebugtool

import akka.actor.{Props, _}
import com.github.unknownnpc.remotedebugtool.actor.MainAppActor
import com.github.unknownnpc.remotedebugtool.message.{MainAppActorStart, MainAppActorStop}
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object Main extends App {

  private implicit val actorSystem = ActorSystem("remote-debug-tool")
  private val log = LoggerFactory.getLogger(this.getClass)
  private val mainActorRef = createMainAppActorRef()

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

  private def createMainAppActorRef() = {
    actorSystem.actorOf(Props(new MainAppActor()), name = "main-app-actor")
  }

}
