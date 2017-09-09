package com.draugrsoft.integration.helper.routes

import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport.defaultNodeSeqMarshaller
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.actor._
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

import com.draugrsoft.integration.helper.actors.MasterJobActor._
import com.draugrsoft.integration.helper.actors._

import scala.concurrent.Await

import com.draugrsoft.integration.helper.actors.MasterJobActor._
import com.draugrsoft.integration.helper.constants.JobStatus._
import com.draugrsoft.integration.helper.constants.MessageLevel.MessageLevelEnum

import akka.stream.ActorMaterializer

import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages._
import com.draugrsoft.integration.helper.constants.JobAction._
import com.draugrsoft.integration.helper.connector.IntegrationHelperConnector
import com.draugrsoft.integraiton.helper.marshallers.IntegrationMarshalling

/**
 * Routes can be defined in separated classes like shown in here
 */
trait IntegrationRoutes extends IntegrationHelperConnector with IntegrationMarshalling {


  import StatusCodes._

import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages._

  val integration = "integration"
  val job = "job"

  lazy val integrationRoutes: Route = integrationsWithNoName ~ integrationsWithName

  lazy val integrationsWithNoName =
    pathPrefix(integration) {
      pathEndOrSingleSlash {
        get {
          onSuccess(getIntegrationsInfo) {
            integrations => complete(OK, integrations)
          }
        } ~
          delete {
            onSuccess(stopIntegrations) { b =>
              complete(OK)
            }
          } ~
          patch {
            complete(NotFound) //TODO
          } ~
          post {
            complete(MethodNotAllowed)
          } ~
          put {
            complete(MethodNotAllowed)
          }

      }
    }

  lazy val integrationsWithName =
    pathPrefix(integration / Segment) { integrationName =>
      {
        pathEndOrSingleSlash {
          get {
            onSuccess(getJobsInfo(integrationName)) {
              _.fold(complete(NotFound))(integration => complete(OK, integration))
            }
          } ~
            delete {
              onSuccess(stopIntegration(integrationName)) { b =>
                complete(OK)
              }
            } ~
            patch {
              entity(as[JobAction]) { action =>
                {
                  onSuccess(integrationAction(integrationName, action)) {
                    updateResponse =>
                      complete(OK, updateResponse)
                  }
                }
              }
            } ~
            put {
              complete(MethodNotAllowed)
            } ~
            post {
              complete(MethodNotAllowed)
            }
        } ~
          jobRoutesWithName(integrationName) ~ jobRoutesWithoutName(integrationName)

      }
    }

  /**
   * Can be overwritten
   */
  def jobRoutesWithName(integrationName: String): Route =
    pathPrefix(job / Segment) { jobName =>
      {
        pathEndOrSingleSlash {
          get {
            onSuccess(getJobInfo(integrationName, jobName)) {
              _.fold(complete(NotFound))(jd => complete(OK, jd))
            }
          } ~
            delete {
              onSuccess(stopJob(integrationName, jobName)) {
                _.fold(complete(NotFound))(jid => complete(OK, jid))
              }
            } ~
            patch {
              entity(as[JobAction]) { action =>
                {
                  onSuccess(jobAction(integrationName, jobName, action)) { _.fold(complete(NotFound))(jar => complete(OK, jar)) }
                }
              }
            }

        }
      }
    }

  /**
   * Can be overwritten
   */
  def jobRoutesWithoutName(integrationName: String): Route =
    pathPrefix(job) {
      pathEndOrSingleSlash {
        get {
          onSuccess(getJobsInfo(integrationName)) {
            _.fold(complete(NotFound))(is => complete(OK, is))
          } ~
            delete {
              onSuccess(stopJobs(integrationName)) { responses =>
                {
                  if (responses.isEmpty) {
                    complete(NotFound)
                  } else {
                    complete(OK, responses)
                  }
                }
              }
            }
        }

      }
    }
}
