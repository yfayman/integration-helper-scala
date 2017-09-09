package com.draugrsoft.integration.helper.constants

/**
 * The actions a job can take
 */
object JobAction {
  sealed abstract class JobActionEnum(name: String) {
    override def toString = name
  }

  case object StartAction extends JobActionEnum("START")
  case object StopAction extends JobActionEnum("STOP")

  implicit def convertJobActionEnumToString(jae: JobActionEnum): String = jae.toString

  implicit def convertStringToJobActionEnum(str: String): JobActionEnum = {
    str match {
      case "START" => StartAction
      case "STOP"  => StopAction
    }
  }
  
}