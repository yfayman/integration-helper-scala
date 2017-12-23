package com.draugrsoft.integration.helper.actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import com.draugrsoft.integration.helper.constants.JobStatus._
import com.draugrsoft.integration.helper.constants.JobAction._
import scala.util.Either
import akka.pattern.ask
import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages._
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import akka.event.Logging
import com.draugrsoft.integration.helper.constants.MessageLevel.MessageLevelEnum
import scala.util.{ Success, Failure }
import akka.actor.Status

private[integration] object MasterJobActor {

  //Stuff sent to MasterJobActor from entrypointActor
  case class LogAttribute(name: String, value: String)
  case class JobMessage(msg: String, level: MessageLevelEnum)
  case class SendResult(attribute: Map[String, String], messages: List[JobMessage])

  def props(dispatcherProps: Props, name: String, dataStoreActor: ActorRef): Props =
    Props(classOf[MasterJobActor], Left(dispatcherProps), name, dataStoreActor)

  def props(dispatcherRef: ActorRef, name: String, dataStoreActor: ActorRef): Props =
    Props(classOf[MasterJobActor], Right(dispatcherRef), name, dataStoreActor)
}

private[integration] class MasterJobActor(actorInfo: Either[Props, ActorRef], name: String, dataStoreActor: ActorRef) extends Actor
  with ActorDispatcherExecutionContext
  with FiveSecondTimeout {

  import MasterJobActor._

  val log = Logging(context.system, this)
  
  def getInitStatus = JobInstanceData(id = None, 
                                      name, 
                                      start =None,
                                      end = None, 
                                      params = Nil,
                                      messages = Nil,
                                      attributes = Map(), 
                                      INITIALIZING)

  log.info(s"Master Job Actor for $name started")

  val dispatcherActor = actorInfo match {
    case Left(props) => context.actorOf(props)
    case Right(ref)  => ref
  }

  var currentData: JobInstanceData = getInitStatus

  def receive = {
    case HistoricalData(data) => {
      data.foreach(jid => dataStoreActor ! SaveDataRequest(jid))
    }
    case JobAction(action, params) => {
      action match {
        case StartAction => {
          if (currentData.status == COMPLETED) {
            dataStoreActor ! SaveDataRequest(currentData)
            currentData = getInitStatus // reset this
          }else if(currentData.status != RUNNING) {
            dispatcherActor ! JobAction(action, params)

            currentData = currentData.copy(
                status = RUNNING,
                start = Some(System.currentTimeMillis()),
                params = params ::: currentData.params
            )
          }else{
            // If you're here, the job is running. You don't want to start it again
          }
        }
        case StopAction => {
          if (currentData.status == RUNNING) {
            dispatcherActor ! JobAction(action, params)
            currentData = currentData.copy(
                status = STOPPED,
                end = Some(System.currentTimeMillis),
                params = params ::: currentData.params
            )
          }else{
            // If it's not running, then there's no point in stopping
          }
        }
      }
      context.parent ! UpdateStatusRequest(self, currentData.status)
      sender ! UpdateJobResponse(currentData)    
    }
    case jsr: JobStatusRequest => {
      sender ! JobStatusResponse(Some(currentData))
    }
    case jsr: JobStatiRequest => {
      val statiRequestor = sender()
      dataStoreActor.ask(GetHistoricalInfoRequest).mapTo[GetHistoricalInfoResponse]
        .onComplete({
          case Success(ghir) => statiRequestor ! JobStatiResponse(currentData :: ghir.historicalData)
          case Failure(e)    =>   context.parent ! Status.Failure(e)
        })
    }

    //Messages that come from dispatcher actor
    case JobMessage(msg, level) =>
      currentData = currentData.copy(messages = JobMessage(msg, level) :: currentData.messages)
    case LogAttribute(name, value) =>
      currentData = currentData.copy(attributes = currentData.attributes + (name -> value))
    case SendResult(attributes, messages) => 
      currentData = currentData.copy(
          end = Some(System.currentTimeMillis()),
          status = COMPLETED,
          attributes = currentData.attributes ++ attributes,
          messages = messages ::: currentData.messages
       )
    case _ => ()
  }
}