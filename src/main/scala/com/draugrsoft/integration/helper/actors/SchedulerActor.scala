package com.draugrsoft.integration.helper.actors

import akka.actor._
import akka.event.Logging
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import scala.concurrent.duration._
import scala.util.{Try,Success,Failure}
import com.draugrsoft.integration.helper.cron.NextRunCalculator

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
         context.system.scheduler.scheduleOnce(NextRunCalculator(schedule), context.parent, Trigger(schedule))
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
    case Trigger(sched) => {
      context.parent ! TriggerOnce
      sched match {
        case ScheduleCronTrigger(sec,min,hour,dom,dow,month) =>
        case ScheduleSecondTrigger(sec) =>
        case _ => //If it's anything but a cron trigger, nothing needs to be done

      }
    }
    case unknown => log.info(s"Received unknown msg type $unknown")
  }

  
}