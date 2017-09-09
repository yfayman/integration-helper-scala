package com.draugrsoft.integration.helper.messages

import com.draugrsoft.integration.helper.constants.JobStatus.JobStatusEnum
import com.draugrsoft.integration.helper.constants.MessageLevel.MessageLevelEnum
import com.draugrsoft.integration.helper.constants.JobStatus.JobStatusEnum
import akka.actor.ActorRef
import com.draugrsoft.integration.helper.constants.JobAction.JobActionEnum

/**
 * Messages that are sent to both MasterIntegration and MasterJob actors
 */
object CommonActorMessages {

  /* Provided by Client Job that sits under the JobMasterActor */
  case class JobInstanceData(id: Int, name: String, start: Option[Long], end: Option[Long], messages: List[JobMessage], attribute: List[JobAttribute], status: JobStatusEnum)
  case class JobMessage(msg: String, level: MessageLevelEnum)
  case class JobAttribute(name: String, value: String)
  case class HistoricalData(data: List[JobInstanceData])


  case object JobNotFound
  case class UpdateJobResponse(data: JobInstanceData)
  case class JobStatusResponse(data: Option[JobInstanceData])
  case class JobStatiResponse(data: List[JobInstanceData])
  case class JobRecentStatus(name: String, status: JobStatusEnum)
  case class IntegrationRecentStatus(name: String, jobs: List[JobRecentStatus])

  case class UpdateJobsRequest(action: JobActionEnum) // This is bound to API

  //This applies to a single job. Name included to allow MasterIntegrationActor to select the right MasterJobActor
  case class UpdateJobRequest(name: String, action: JobActionEnum)
  //For getting current data
  case class JobStatusRequest(name: String)
  //For getting historical and current data
  case class JobStatiRequest(name: String)
  case object IntegrationStatusRequest
  case class UpdateStatusRequest(job: ActorRef, status: JobStatusEnum)
  case class JobAction(action: JobActionEnum)

}