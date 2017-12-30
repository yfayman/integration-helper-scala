package com.draugrsoft.integration.helper.messages

import com.draugrsoft.integration.helper.constants.JobStatus.JobStatusEnum
import com.draugrsoft.integration.helper.constants.MessageLevel.MessageLevelEnum
import com.draugrsoft.integration.helper.constants.JobStatus.JobStatusEnum
import akka.actor.ActorRef
import com.draugrsoft.integration.helper.constants.JobAction.JobActionEnum
import com.draugrsoft.integration.helper.actors.MasterJobActor._

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
  case class UpdateJobResponse(data: JobInstanceData)
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

  // Messages sent to MasterDataActor
  case class SaveDataRequest(data: JobInstanceData)
  case class SaveDataResponse(id: Int, error:Option[String])
  case object GetHistoricalInfoRequest
  case class GetHistoricalInfoResponse(historicalData: List[JobInstanceData])

}