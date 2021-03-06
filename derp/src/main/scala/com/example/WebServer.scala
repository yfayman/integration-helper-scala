package com.draugrsoft.integraiton.helper
/*
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.io.StdIn
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

import scala.io.StdIn
import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages._
import akka.actor._
import com.draugrsoft.integration.helper.routes.IntegrationRoutes
import com.draugrsoft.integration.helper.routes.BaseRoutes
import com.typesafe.config.ConfigFactory
import com.draugrsoft.integration.helper.store.DataStore

object WebServer extends Directives with IntegrationRoutes {  
  
  val ints = Integration("integration1", JobWithProps("jerbOne",DummyJobActorWS.props) :: JobWithProps("jerbTwo", DummyJobActorWS.props) :: Nil)

  override def integrations: List[Integration] = ints :: Nil

 
  override implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  override implicit val system = ActorSystem("http-system")
  override implicit val materializer = ActorMaterializer()
  
  override implicit val ec = system.dispatcher

  def main(args: Array[String]) {

    // needed for the future flatMap/onComplete in the end
    
    val config = ConfigFactory.load()
    val wsConfig = config.getConfig("ws-config")
    val port = wsConfig.getInt("port")
    val uri = wsConfig.getString("uri")

    val bindingFuture = Http().bindAndHandle(routes, uri, port)

    println(s"Server online at http://$uri:$port/\nPress RETURN to stop...")

    
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  // Here you can define all the different routes you want to have served by this web server
  // Note that routes might be defined in separated traits like the current case
  lazy val routes = BaseRoutes.baseRoutes ~ integrationRoutes

}

object DummyJobActorWS {
  def props: Props = Props(classOf[DummyJobActorWS])
}

class DummyJobActorWS extends Actor {
  def receive = {
    case _ => ()
  }
}
*/

