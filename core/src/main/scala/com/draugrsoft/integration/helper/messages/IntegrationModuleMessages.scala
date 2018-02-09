package com.draugrsoft.integration.helper.messages

import akka.actor.ActorRef
import com.draugrsoft.integration.helper.constants.JobStatus.JobStatusEnum
import akka.actor.Props
import com.draugrsoft.integration.helper.constants.JobStatus.JobStatusEnum
import com.typesafe.config.Config
import com.draugrsoft.integration.helper.store.DataStore
import com.draugrsoft.integration.helper.messages.CommonActorMessages.HistoricalData
import com.draugrsoft.integration.helper.store.DefaultJobInstanceDataStore

object IntegrationModuleMessages {

  /* Every integration runs within it's own Actor System and has a 
   * dispatcher who will communicate with API user provided actors
   */
  case class IntegrationMetaData(integration: Integration, dispatcherActor: ActorRef)
  case class JobMetaData(job: Job, jobMasterActor: ActorRef, status: JobStatusEnum)
  case class Integration(name: String, jobs: List[Job], store: DataStore = DefaultJobInstanceDataStore)

  /*
   * dispatcherPros - the main entry point for the job
   */
  case class JobWithProps(override val name: String, dispatcherPros: Props) extends Job(name)
  case class JobWithRef(override val name: String, ref: ActorRef) extends Job(name)

  sealed abstract class Job(val name: String)
}