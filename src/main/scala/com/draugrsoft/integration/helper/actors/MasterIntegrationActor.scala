package com.draugrsoft.integration.helper.actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import akka.pattern.ask
import scala.language.postfixOps
import com.draugrsoft.integration.helper.constants.JobStatus._
import scala.concurrent.Future
import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages._
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import akka.event.Logging

private[integration] object MasterIntegrationActor {

  def props(integration: Integration): Props = Props(classOf[MasterIntegrationActor], integration)

}

private[integration] class MasterIntegrationActor(integration: Integration) extends Actor
  with FiveSecondTimeout
  with ActorDispatcherExecutionContext {

  import MasterIntegrationActor._

  val log = Logging(context.system, this)

  val dataStoreRef = context.actorOf(MasterDataActor.props(integration.store))

  log.info(s"Started MasterIntegrationActor for $integration.name")

  /**
   * name -> JobMetaData
   *
   * JobMetaData contains information regarding the client actor, mastor actor and current status
   */
  var jobMap: Map[String, JobMetaData] = integration.jobs.map(job => {

    val jobMasterActor = job match {
      case JobWithProps(name, props) => context.actorOf(MasterJobActor.props(props, name, dataStoreRef))
      case JobWithRef(name, ref)     => context.actorOf(MasterJobActor.props(ref, name, dataStoreRef))
    }

    JobMetaData(job, jobMasterActor, INITIALIZING)
  }).groupBy { jmd => jmd.job.name }
    .mapValues { _.head }

  val name = integration.name

  def receive = {
    case UpdateStatusRequest(actor, status) => {
      val kvToReplace = jobMap.find(_._2.jobMasterActor == actor)
      if (kvToReplace.isDefined) {
        val kv = kvToReplace.get
        val jobName = kv._1
        val jmd = kv._2.copy(status = status)
        jobMap = jobMap + (jobName -> jmd)
      }
    }
    case IntegrationStatusRequest => {
      val stati = jobMap.values.map(jmd => JobRecentStatus(jmd.job.name, jmd.status)).toList
      sender() ! IntegrationRecentStatus(name, stati)
    }
    case UpdateJobsRequest(action) => {
      val requestor = sender()
      val reply = Future.sequence { jobMap.values.map { jmd => jmd.jobMasterActor.ask(JobAction(action, Nil)).mapTo[UpdateJobResponse] } }
      reply.map { items => requestor ! items.toList }
    }
    case UpdateJobRequest(jobName, action) => {
      val requestor = sender()
      jobMap.get(jobName).fold(sender() ! JobNotFound)(jmd => {
        jmd.jobMasterActor.ask(action).mapTo[UpdateJobResponse].map { updateResponse => requestor ! updateResponse }
      })
    }
    case jsr: JobStatusRequest => {
      val requestor = sender
      jobMap.get(jsr.name).fold(requestor ! JobStatusResponse(None))(jmd => {
        val responseFuture = jmd.jobMasterActor.ask(jsr).mapTo[JobStatusResponse]
        responseFuture.map { res => requestor ! res }
      })
    }

    case jsr: JobStatiRequest => {
      val statiRequestor = sender()

      jobMap.get(jsr.name).fold(statiRequestor ! JobStatiResponse(Nil))(jmd => {
        jmd.jobMasterActor.ask(jsr).mapTo[JobStatiResponse]
          .map { res => statiRequestor ! res }
      })
    }

    case msg => log.warning(s"Received an unknown message $msg")
  }

}