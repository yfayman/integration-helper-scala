package com.draugrsoft.integration.helper.connector

import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages._
import com.draugrsoft.integration.helper.constants.JobAction._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.actor._
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.util.Timeout
import com.draugrsoft.integration.helper.actors._
import scala.concurrent.Await
import com.draugrsoft.integration.helper.actors.MasterJobActor._
import com.draugrsoft.integration.helper.constants.JobStatus._
import com.draugrsoft.integration.helper.constants.MessageLevel.MessageLevelEnum
import akka.stream.ActorMaterializer
import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages._
import com.draugrsoft.integration.helper.constants.JobAction._
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages._

trait IntegrationHelperConnector {
  
  /**
   *  Things that need to be overridden
   */

  /**
   * Example with 2 jobs(One using props, and another using ref)
   * Integration("integration1", JobWithProps("jerbOne", DummyJobActor.props) :: JobWithRef("jerbTwo", actorRef) :: Nil) :: Nil
   */
  def integrations: List[Integration]
  
  implicit val timeout: Timeout
  implicit val ec: ExecutionContext
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  def getJobInfo(integrationName: String, jobName: String): Future[Option[JobInstanceData]] =
    integrationMap.get(integrationName)
                  .fold[Future[Option[JobInstanceData]]](Future.successful(None)) {
                      _.dispatcherActor.ask(JobStatusRequest(jobName))
                                        .mapTo[JobStatusResponse]
                                        .map { _.data }
    }

  def stopJob(integrationName: String, jobName: String): Future[Option[JobInstanceData]] = {
    integrationMap.get(integrationName).fold[Future[Option[JobInstanceData]]](Future.successful(None)){
      _.dispatcherActor.ask(UpdateJobRequest(jobName,JobAction(StopAction , Nil)))
                       .mapTo[UpdateJobResponse]
                       .map(_.data)
    }
    
   
  }

  def stopJobs(integrationName: String): Future[List[UpdateJobResponse]] = {
    integrationMap.get(integrationName).fold[Future[List[UpdateJobResponse]]](Future.successful(Nil)) {
      _.dispatcherActor.ask(UpdateJobsRequest(StopAction))
                        .mapTo[List[UpdateJobResponse]]
    }
  }

  /**
   * returns the status of every job in an integration. This will only return  the status of the
   * current/most recent job instance
   * @param integrationName The name of the integration to retrieve stati for
   */
  def getJobsInfo(integrationName: String): Future[Option[IntegrationRecentStatus]] = {
    integrationMap.get(integrationName).fold[Future[Option[IntegrationRecentStatus]]](Future.successful(None)) {
      _.dispatcherActor.ask(IntegrationStatusRequest)
                        .mapTo[IntegrationRecentStatus]
                        .map(irs => Some(irs))
    }
  }

  /**
   * this gets the most recent job status for every integration
   */
  def getIntegrationsInfo: Future[List[IntegrationRecentStatus]] = {
    Future.sequence {
      integrationMap.values.map {
        _.dispatcherActor.ask(IntegrationStatusRequest)
                          .mapTo[IntegrationRecentStatus]
      }
    }.map(_.toList)
  }

  /**
   * This applies an action to all the jobs. For example, you may want to stop all the jobs 
   * for integration A
   * @param name The integration to which the action applies
   * @param the action that will apply to all jobs in an integration
   */
  def integrationAction(name: String, jobAction: JobAction): Future[List[UpdateJobResponse]] = {
    integrationMap.get(name).fold[Future[List[UpdateJobResponse]]](Future.successful(Nil)) {
      _.dispatcherActor.ask(UpdateJobsRequest(jobAction.action))
                        .mapTo[List[UpdateJobResponse]]
    }
  }

  /**
   * This applies an action to a specific job in an integration
   * @param integrationName the integration to which the job belongs
   * @param jobName the job to which the action will be applied
   * @param jobAction the action to be applied
   */
  def jobAction(integrationName: String, jobName: String, jobAction: JobAction): Future[Option[UpdateJobResponse]] = {
    integrationMap.get(integrationName).
      fold[Future[Option[UpdateJobResponse]]](Future.successful(None)) {
        _.dispatcherActor.ask(UpdateJobRequest(jobName, jobAction))
                          .mapTo[UpdateJobResponse]
                          .map(ujar => Some(ujar))
      }
  }

  //TODO do something more useful with this
  def stopIntegrations: Future[Boolean] = {
    integrationMap.values.map { _.dispatcherActor ! PoisonPill }
    Future.successful(true)
  }

  /**
   * This will send a PoisonPill to the MasterIntegrationActor. This will
   * always return True if the Integration exists
   * @param name The name of the integration you want to stop
   */
  def stopIntegration(name: String): Future[Boolean] = {
    integrationMap.get(name).fold(Future.successful(false))(
        imd => {
          imd.dispatcherActor ! PoisonPill
          Future.successful(true)
        })
  }

  private lazy val integrationMap = integrations.map(in =>  (in.name, initializeIntegration(in)))
                                                .toMap

  private def initializeIntegration(integration: Integration): IntegrationMetaData = {
    val mainIntegrationActor = system.actorOf(MasterIntegrationActor.props(integration))
    IntegrationMetaData(integration, mainIntegrationActor)
  }
}