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
  var currentData: JobInstanceData = JobInstanceData(0, name, None, None, Nil, Nil, INITIALIZING)
  var historicalData: List[JobInstanceData] = Nil

  def receive = runningState

  def runningState: PartialFunction[Any, Unit] = {
    case HistoricalData(data) => {
      historicalData = data
    }
    case JobAction(action, params) => {
      action match {
        case StartAction => {
          if (currentData.status != RUNNING) {
            dispatcherActor ! action
            currentData = currentData.copy(status = RUNNING, start = Some(System.currentTimeMillis()))
          }
        }
        case StopAction => {
          if (currentData.status == RUNNING) {
            dispatcherActor ! action
            currentData = currentData.copy(status = STOPPED, end = Some(System.currentTimeMillis()))
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
    case _                    => ()
  }

}