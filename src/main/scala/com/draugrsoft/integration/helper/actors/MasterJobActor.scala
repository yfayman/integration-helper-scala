package com.draugrsoft.integration.helper.actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import com.draugrsoft.integration.helper.constants.JobStatus._
import com.draugrsoft.integration.helper.constants.JobAction._
import scala.util.Either

object MasterJobActor {

  def props(dispatcherProps: Props, name: String): Props = Props(classOf[MasterJobActor], Left(dispatcherProps), name)
  def props(ref: ActorRef, name: String): Props = Props(classOf[MasterJobActor], Right(ref), name)
}

class MasterJobActor(actorInfo: Either[Props, ActorRef], name: String) extends Actor {

  import MasterJobActor._
  import com.draugrsoft.integration.helper.messages.CommonActorMessages._

  val dispatcherActor = actorInfo match {
    case Left(props) => context.actorOf(props)
    case Right(ref)  => ref
  }
  // We initialize the job instance id to 0, because we don't know about historical data yet
  var currentData: JobInstanceData = getInitStatus 
  var historicalData: List[JobInstanceData] = Nil

  def receive = runningState

  def runningState: PartialFunction[Any, Unit] = {
    case HistoricalData(data) => {
      historicalData = data
    }
    case ja: JobAction => {
      ja.action match {
        case StartAction => {
          if(currentData.status == COMPLETED){
            historicalData = currentData :: historicalData
            currentData = getInitStatus // reset this
          }
          if (currentData.status != RUNNING) {
            dispatcherActor ! ja
            currentData = currentData.
              copy(status = RUNNING,
                start = Some(System.currentTimeMillis()),
                params = ja.params.getOrElse(Nil) ::: currentData.params)
          }
        }
        case StopAction => {
          if (currentData.status == RUNNING) {
            dispatcherActor ! ja
            currentData = currentData.
              copy(status = STOPPED,
                end = Some(System.currentTimeMillis()),
                params = ja.params.getOrElse(Nil) ::: currentData.params)
          }
        }
      }
      context.parent ! UpdateStatusRequest(self, currentData.status)
      sender() ! UpdateJobResponse(currentData)
    }
    case jsr: JobStatusRequest => {
      sender ! JobStatusResponse(Some(currentData))
    }
    case jsr: JobStatiRequest => sender ! JobStatiResponse(currentData :: historicalData)

    //Messages that come from dispatcher actor
    case JobMessage(msg, level) =>
      currentData = currentData.
        copy(messages = JobMessage(msg, level) :: currentData.messages)
    case LogAttribute(name, value) =>
      currentData = currentData.copy(attributes =  currentData.attributes + (name -> value))
    case SendResult(attributes, messages) => {
      currentData = currentData
        .copy(end = Some(System.currentTimeMillis()),
          status = COMPLETED,
          attributes = currentData.attributes ++ attributes,
          messages = messages ::: currentData.messages)
    }
    case _ => ()
  }
  
  def getInitStatus = JobInstanceData(0, name, None, None, Nil, Nil, Map(), INITIALIZING)
}