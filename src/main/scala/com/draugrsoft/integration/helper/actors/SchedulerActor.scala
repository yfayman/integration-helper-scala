package com.draugrsoft.integration.helper.actors

import akka.actor._
import akka.event.Logging
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import scala.concurrent.duration._
import scala.util.{Try,Success,Failure}

object SchedulerActor {

  def props(dataStoreActor: ActorRef): Props = Props(classOf[SchedulerActor], dataStoreActor)



}

class SchedulerActor(dataStoreActor: ActorRef) extends Actor  
  with FiveSecondTimeout with ActorDispatcherExecutionContext { 

  
  val log = Logging(context.system, this)

  var map: Map[Schedule, Cancellable] = Map.empty

  def receive = {
    case GetSchedule => sender ! GetScheduleResponse(map.keys.toList)
    case AddSchedule(schedule) => {    
       val futureTaskTry = Try {
         context.system.scheduler.scheduleOnce(delayCalc(schedule), context.parent, Trigger)
       }
       
       futureTaskTry match {
         case Success(futureTask) => {
           map = map + (schedule -> futureTask)
           sender ! AddScheduleResponse()
         }
         case Failure(throwable) => throwable match{
           case ex:Exception => sender ! AddScheduleResponse(Some(ex))
           case error:Error => throw error
         }
       }
       
    }
    case RemoveSchedule(schedule) => {
      map.get(schedule).fold(sender ! ScheduleNotFound)(c => {
        sender ! RemoveScheduleResponse()
      })
    }
    case Trigger => context.parent ! Trigger
    case a => log.info(s"Received unknown msg type $a")
  }
  
  /**
   * Calculates the delay from the current time in order to ensure that
   * the action occurs at the right time
   */
  def delayCalc(sched: Schedule) : FiniteDuration = FiniteDuration(10,SECONDS) //TODO temporary implementation

}