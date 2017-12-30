package com.draugrsoft.integration.helper.constants


/**
 * Similar to Quartz. Describes how when an event occurs and 
 * if it is to be repeated in some manner. 
 */
private [integration] object Trigger {
  
  
  sealed trait TriggerEnum
  
  /**
   * Runs on a schedule(at specific times)
   */
  case object CronTrigger extends TriggerEnum
  
  
  /**
   * Runs every X seconds 
   */
  case object SecondTrigger extends TriggerEnum
  
  
  /**
   * Runs once
   */
  case object SingleTrigger extends TriggerEnum
  
  
}