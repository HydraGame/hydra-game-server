package com.hydra.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.hydra.server.galaxy.GalaxyJsonProtocol._
import scala.io.StdIn
import akka.http.scaladsl.server.Directives.handleWebSocketMessages
import com.hydra.server.command.AttackCommand
import spray.json._
import scala.concurrent.Future

object Server extends App {

  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  val interface = "localhost"
  val port = 8081

  import akka.http.scaladsl.server.Directives._

  Future { Game.run() }

  val handleIncomingMessage = (msg: String) => msg match {
    case "get-galaxy" =>
      val galaxyState: Option[String] = Game.redisClient.get("galaxy-simple-json-Andromeda")
      TextMessage(galaxyState.getOrElse("{\"error\": \"Cannot get galaxy\"}"))
    case "attack" => Game.redisClient.lpush("commands", AttackCommand("Abelsky", "Abemasanao").toJson); TextMessage("{\"error\": \"No response\"}")
    case _ => TextMessage("{\"error\": \"Incorrect command\"}")
  }

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => println(s"Received $txt"); handleIncomingMessage(txt)
    case _ => TextMessage("Message type unsupported")
  }

  val route = get {
    pathEndOrSingleSlash {
      handleWebSocketMessages(echoService)
    }
  }

  val binding = Http().bindAndHandle(route, interface, port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()

  binding.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  println("Server is down...")



}