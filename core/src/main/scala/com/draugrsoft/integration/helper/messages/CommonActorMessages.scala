package com.draugrsoft.integration.helper.messages

import com.draugrsoft.integration.helper.constants.JobStatus.JobStatusEnum
import com.draugrsoft.integration.helper.constants.MessageLevel.MessageLevelEnum
import akka.actor.ActorRef
import com.draugrsoft.integration.helper.constants.JobAction.JobActionEnum
import com.draugrsoft.integration.helper.actors.MasterJobActor._
import com.draugrsoft.integration.helper.constants.Trigger._
import com.draugrsoft.integration.helper.constants.Cron._

/**
 * Messages used for interaction with Actors in this Project. These should not be
 * visible to the client
 */
private[integration] object CommonActorMessages {

  /* Provided by Client Job that sits under the JobMasterActor */
  case class JobInstanceData(id: Option[Int],
                            name: String, 
                            start: Option[Long],
                            end: Option[Long],
                            params: List[JobParam], 
                            messages: List[JobMessage],
                            attributes: Map[String, String], 
                            status: JobStatusEnum)

  // case class JobAttribute(name: String, value: String)
  case class HistoricalData(data: List[JobInstanceData])

  case object JobNotFound
  case class UpdateJobResponse(data: Option[JobInstanceData])
  case class JobStatusResponse(data: Option[JobInstanceData])
  case class JobStatiResponse(data: List[JobInstanceData])
  case class JobRecentStatus(name: String, status: JobStatusEnum)
  case class IntegrationRecentStatus(name: String, jobs: List[JobRecentStatus])

  case class UpdateJobsRequest(action: JobActionEnum) // This is bound to API

  //This applies to a single job. Name included to allow MasterIntegrationActor to select the right MasterJobActor
  case class UpdateJobRequest(name: String, action: JobAction)
  //For getting current data
  case class JobStatusRequest(name: String)
  //For getting historical and current data
  case class JobStatiRequest(name: String)
  case object IntegrationStatusRequest

  // Messages sent to jobs
  case class JobParam(name: String, value: String)
  case class UpdateStatusRequest(job: ActorRef, status: JobStatusEnum)
  case class JobAction(action: JobActionEnum, params: List[JobParam]) 

  // Scheduling
  sealed abstract class Schedule(trigger:TriggerEnum)
  //Triggers once
  case class ScheduleSingleTrigger(time:Long) extends Schedule(SingleTrigger) 
  //Triggers every X seconds
  case class ScheduleSecondTrigger(secondInterval:Long) extends Schedule(SecondTrigger)
  
  case class ScheduleCronTrigger(seconds:CronSeconds,
                                 minutes:CronMinutes,
                                 hours:CronHours,
                                 dayOfMonth:CronDayOfMonth,
                                 dayOfWeek:CronDayOfWeek,
                                 month:CronMonth) extends Schedule(CronTrigger)
  
  /**
   * For interacting with persisted schedules
   */
  case object GetSchedule
  case class GetScheduleResponse(xs:List[Schedule])
  case class AddSchedule(sched:Schedule)
  case class AddScheduleResponse(e:Option[Exception] = None) 
  case class RemoveSchedule(sched:Schedule)
  case class RemoveScheduleResponse(e:Option[Exception] = None)
  case object ScheduleNotFound
  
  
  // generates a TriggerOnce and reschedules depending on what type of schedule
  case class Trigger(sched:Schedule) 
  // Sent to master job actor
  case object TriggerOnce 
  
  
  // Messages sent to MasterDataActor
  case class SaveDataRequest(data: JobInstanceData)
  case class SaveDataResponse(idOrError:Either[Int,Exception])
  case object GetHistoricalInfoRequest
  case class GetHistoricalInfoResponse(historicalData: List[JobInstanceData])

}