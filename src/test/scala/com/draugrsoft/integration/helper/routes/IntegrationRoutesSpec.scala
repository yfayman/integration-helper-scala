package com.draugrsoft.integration.helper.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{ Matchers, WordSpec }
import akka.util.Timeout
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import java.util.concurrent.TimeUnit
import akka.actor._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import com.draugrsoft.integration.helper.marshallers.IntegrationMarshalling
import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages._
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import com.draugrsoft.integration.helper.constants.JobStatus._
import com.draugrsoft.integration.helper.actors.DummyActor

class IntegrationRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest with IntegrationMarshalling {

  class IntegrationTest extends IntegrationRoutes {
    override def integrations: List[Integration] = List(testIntegration)

    override implicit val timeout = Timeout(50, TimeUnit.SECONDS)

    override implicit val system = ActorSystem("http-system")
    override implicit val materializer = ActorMaterializer()
    override implicit val ec = system.dispatcher

    lazy val testIntegration = Integration("integration1", JobWithProps("jerbOne", DummyActor.props) :: JobWithProps("jerbTwo", DummyActor.props) :: Nil)

  }

  val routes = new IntegrationTest

  "Integration Route" should {

    "get /integration/integration1 responds with Ok status and correct data " in {
      Get("/" + routes.integration + "/integration1") ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[IntegrationRecentStatus] shouldBe IntegrationRecentStatus("integration1", JobRecentStatus("jerbTwo", INITIALIZING) :: JobRecentStatus("jerbOne", INITIALIZING) :: Nil)
      }
    }

    "get /integration/integration1/ responds with Ok status and correct data" in {
      Get("/" + routes.integration + "/integration1/") ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[IntegrationRecentStatus] shouldBe IntegrationRecentStatus("integration1", JobRecentStatus("jerbTwo", INITIALIZING) :: JobRecentStatus("jerbOne", INITIALIZING) :: Nil)
      }
    }

    "get /integration/junk responds with NotFound status" in {
      Get("/" + routes.integration + "/junk") ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "patch /integration/intergration1 without params responds with Ok status" in {
      val httpEntity = HttpEntity(ContentTypes.`application/json`, "{\"action\" : \"START\" }")
      Patch("/integration/integration1", httpEntity) ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "patch /integration/intergration1 with params responds with Ok status" in {
      val httpEntity = HttpEntity(ContentTypes.`application/json`, "{\"action\" : \"START\" ,\"params\" : [ {\"name\":\"param1Name\", \"value\":\"param1Val\" }]}")
      Patch("/integration/integration1", httpEntity) ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "patch /integration/intergration1/ responds with Ok status" in {
      val httpEntity = HttpEntity(ContentTypes.`application/json`, "{\"action\" : \"START\"}")
      Patch("/integration/integration1/", httpEntity) ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "put /integration responds with MethodNotAllowed status" in {
      Put("/" + routes.integration) ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.MethodNotAllowed
      }
    }

    "put /integration/ responds with MethodNotAllowed status" in {
      Put("/" + routes.integration + "/") ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.MethodNotAllowed
      }
    }

    "post /integration/ responds with MethodNotAllowed status" in {
      Post("/" + routes.integration + "/") ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.MethodNotAllowed
      }
    }

    "post /integration responds with MethodNotAllowed status" in {
      Post("/" + routes.integration) ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.MethodNotAllowed
      }
    }

    "delete /integration/ responds with Ok Status" in {
      Delete("/" + routes.integration + "/") ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "delete /integration responds with Ok status" in {
      Delete("/" + routes.integration) ~> routes.integrationRoutes ~> check {
        status shouldBe StatusCodes.OK
      }
    }

  }

}