package com.draugrsoft.integration.helper.cron

import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import scala.concurrent.duration._
import com.draugrsoft.integration.helper.constants.Cron._

object NextRunCalculator {
  
  def apply(sched:Schedule):FiniteDuration = apply(System.currentTimeMillis(), sched)
  
  def apply(from:Long,sched:Schedule):FiniteDuration = {
    sched match {
      case ScheduleSingleTrigger(fireTime) => (fireTime - from).millis
      case ScheduleSecondTrigger(sec) => sec.seconds
      case ScheduleCronTrigger(sec, min, hour,dom,dow,mon) => calculateTimeToNextTrigger(from,sec,min,hour,dom,dow,mon)
    }
  }
  
  protected def calculateTimeToNextTrigger(from:Long, 
                                           cronSec:CronSeconds, 
                                           cronMin:CronMinutes, 
                                           cronHour:CronHours, 
                                           cronDom:CronDayOfMonth,
                                           cronDow:CronDayOfWeek,
                                           cronMon:CronMonth):FiniteDuration = ???
  
}