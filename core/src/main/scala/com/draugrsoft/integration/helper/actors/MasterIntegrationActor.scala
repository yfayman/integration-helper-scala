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
import scala.util.{Success,Failure}
import akka.actor.Status

private[integration] object MasterIntegrationActor {

  def props(integration: Integration): Props = Props(classOf[MasterIntegrationActor], integration)

}

private[integration] class MasterIntegrationActor(integration: Integration) extends Actor
  with FiveSecondTimeout
  with ActorDispatcherExecutionContext {

  import MasterIntegrationActor._
  
  type JobName = String

  val log = Logging(context.system, this)

  val dataStoreRef = context.actorOf(MasterDataActor.props(integration.store))

  log.info(s"Started MasterIntegrationActor for $integration.name")

  /**
   * name -> JobMetaData
   *
   * JobMetaData contains information regarding the client actor, mastor actor and current status
   */
  var jobMap: Map[JobName, JobMetaData] = integration.jobs.map(job => {

    val jobMasterActor = job match {
      case JobWithProps(name, props) => context.actorOf(MasterJobActor.props(props, name, dataStoreRef))
      case JobWithRef(name, ref)     => context.actorOf(MasterJobActor.props(ref, name, dataStoreRef))
    }

    JobMetaData(job, jobMasterActor, INITIALIZING)
  }).groupBy { _.job.name }
    .mapValues { _.head }


  /**
   * These messages come from routes. This actor then either gets information from its' state
   * or defers to a JobMasterActor
   */
  def receive = {
    case UpdateStatusRequest(actor, status) => {
      jobMap.find(_._2.jobMasterActor == actor)
            .fold[Unit]{sender ! JobNotFound}{ 
                nameAndMetadata => {
                  val jobName = nameAndMetadata._1
                  val jmd = nameAndMetadata._2.copy(status = status)
                  jobMap = jobMap + (jobName -> jmd)
                }
              }
    }
    case IntegrationStatusRequest => {
      val stati = jobMap.values.map(jmd => JobRecentStatus(jmd.job.name, jmd.status)).toList
      sender ! IntegrationRecentStatus(integration.name, stati)
    }
    case UpdateJobsRequest(action) => {
      val requestor = sender
      Future.sequence { jobMap.values.map { jmd => jmd.jobMasterActor.ask(JobAction(action, Nil)).mapTo[UpdateJobResponse] } }
        .onComplete({
          case Success(items) => requestor ! items.toList 
          case Failure(e) => context.parent ! Status.Failure(e)
        })
      
    }
    case UpdateJobRequest(jobName, action) => {
      val requestor = sender
      jobMap.get(jobName)
            .fold(sender ! UpdateJobResponse(None)){
                _.jobMasterActor.ask(action).mapTo[UpdateJobResponse]
                        .onComplete({
                          case Success(ujr) => requestor ! ujr
                          case Failure(e) =>  context.parent ! Status.Failure(e)
                        })
      }
    }
    case jsr: JobStatusRequest => {
      val requestor = sender
      jobMap.get(jsr.name)
            .fold(requestor ! JobStatusResponse(None)){
                  _.jobMasterActor.ask(jsr).mapTo[JobStatusResponse]
                          .onComplete({
                            case Success(jsr) => requestor ! jsr
                            case Failure (e) =>  context.parent ! Status.Failure(e)
                          })
      }
    }

    case jsr: JobStatiRequest => {
      val statiRequestor = sender

      jobMap.get(jsr.name)
            .fold(statiRequestor ! JobStatiResponse(Nil)){
                _.jobMasterActor.ask(jsr).mapTo[JobStatiResponse]
                      .onComplete({
                        case Success(jsr) => statiRequestor ! jsr
                        case Failure(e) => context.parent ! Status.Failure(e)
                      })
      }
    }

    case msg => log.warning(s"Received an unknown message $msg")
  }

}