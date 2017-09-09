package com.draugrsoft.integration.helper.constants


object MessageLevel {
  sealed abstract class MessageLevelEnum(name:String){
    override def toString = name
  }
  
  case object TRACE extends MessageLevelEnum("TRACE")
  case object DEBUG extends MessageLevelEnum("DEBUG")
  case object INFO extends MessageLevelEnum("INFO")
  case object WARN extends MessageLevelEnum("WARN")
  case object ERROR extends MessageLevelEnum("ERROR")
  
  implicit def convertMessageLevelToString(ml:MessageLevelEnum):String = ml.toString
  
  implicit def convertStringToMessageLevel(str:String):MessageLevelEnum = {
    str match{
      case "TRACE" => TRACE
      case "DEBUG" => DEBUG
      case "INFO" => INFO
      case "WARN" => WARN
      case "ERROR" => ERROR
    }
  }
}
