package com.draugrsoft.integration.helper.constants

private [integration] object JobStatus {
  
  sealed abstract class JobStatusEnum(name:String){
    override def toString = name
  }
  
  case object INITIALIZING extends JobStatusEnum("INITIALIZING") // Initial State when Integration boots up
  case object RUNNING extends JobStatusEnum("RUNNING")
  case object STOPPED extends JobStatusEnum("STOPPED")
  case object COMPLETED extends JobStatusEnum("COMPLETED")

  
  implicit def convertJobStatusToString(jse:JobStatusEnum):String = jse.toString
  
  implicit def convertStringToJobStatus(str:String):JobStatusEnum = {
    str match{
      case "RUNNING" => RUNNING
      case "STOPPED" => STOPPED
      case "INITIALIZING" => INITIALIZING
      case "COMPLETED" => COMPLETED
    }
  }
    
  
}