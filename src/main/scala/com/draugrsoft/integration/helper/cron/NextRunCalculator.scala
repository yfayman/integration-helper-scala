package com.draugrsoft.integration.helper.cron

import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import scala.concurrent.duration._
import com.draugrsoft.integration.helper.constants.Cron._
import java.util.Calendar

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
                                           cronMon:CronMonth):FiniteDuration = {
    
    
    // check to make sure both are not selected
    assert(cronDom.isOmmitted || cronDow.isOmmitted)
    
    val fromCal = Calendar.getInstance;
    fromCal.setTimeInMillis(from)
    
    val fromSec = fromCal.get(Calendar.SECOND)
    val fromMinute = fromCal.get(Calendar.MINUTE)
    val fromHour = fromCal.get(Calendar.HOUR)
    val fromDayOfMonth = fromCal.get(Calendar.DAY_OF_MONTH) 
    val fromDayOfWeek = fromCal.get(Calendar.DAY_OF_WEEK)
    val fromMonth = fromCal.get(Calendar.MONTH) + 1 // Calendar goes 0 to 11. We need 1 to 12
    
    val nextSec = getNextSec(cronSec,fromSec)
    val nextMinute = getNextMin(cronMin,fromMinute)
    val nextHour = getNextHour(cronHour,fromHour)
    val nextDom:Option[Int] = getNextDayOfMonth(cronDom,fromDayOfMonth) 
    val nextDow = getNextDayOfWeek(cronDow,fromDayOfWeek) 
    
    ???
  }
  
  
  /**
   * Calculates the next second component of the next fire. 
   * Ex. If a job fires at 20,40,50 seconds and the fromSec is 30, 
   * then the next one should be 40
   */
  def getNextSec(cronSec:CronSeconds, fromSec:Int) :Int = {
    cronSec match{
      case All => if(fromSec == 59) 0 else fromSec + 1
      case SecondsVal(xs) => {
        val sortedSeconds = xs.sorted
        sortedSeconds.find(_ > fromSec) match {
          case Some(sec) => sec
          case None => sortedSeconds.head
        }
      }
    }
  }
  
  /**
   * Calculates the next minute component of the next fire. 
   * Ex. If a job fires at 20,40,50 seconds and the fromMinute is 30, 
   * then the next one should be 40
   */
  def getNextMin(cronMin:CronMinutes, fromMinute:Int) :Int = {
    cronMin match{
      case All => if(fromMinute == 59) 0 else fromMinute + 1
      case MinutesVal(xs) => {
        val sortedMintues = xs.sorted
        sortedMintues.find(_ > fromMinute) match {
          case Some(min) => min
          case None => sortedMintues.head
        }
      }
    }
  }
  
  
  def getNextHour(cronHour:CronHours, fromHour:Int) :Int = {
  cronHour match {
      case All => if(fromHour == 23) 0 else fromHour + 1
      case HoursVal(xs) => {
        val sortedHours = xs.sorted
        sortedHours.find(_ > fromHour) match{
          case Some(hour) => hour
          case None => sortedHours.head
        }
      }
    }
  }
  
  def getNextDayOfMonth(cronDom:CronDayOfMonth, fromDayOfMonth:Int) :Option[Int] = {
    cronDom match{
      case All => Some {
                    if(fromDayOfMonth == 31) 
                      1 
                    else 
                      fromDayOfMonth + 1 
                  }
      case NoVal => None
      case DayOfMonthVal(xs) => {
        val sortedDoms = xs.sorted
        
        Option {
          sortedDoms.find(_ > fromDayOfMonth) match {
            case Some(dom) => dom
            case None => sortedDoms.head
          }
        }
      }
    }
  }
  
  def getNextDayOfWeek(cronDow:CronDayOfWeek, fromDayOfWeek:Int):Option[Int] = {
    cronDow match{
        case All => Some(fromDayOfWeek + 1)
        case NoVal => None
        case DayOfWeekVal(either) => {
          either match {
            case Left(xs) =>
            case Right(xs) =>
          }
        }
    }
    ???
  }
  
  
}