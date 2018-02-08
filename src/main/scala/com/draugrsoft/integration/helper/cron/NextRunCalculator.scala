package com.draugrsoft.integration.helper.cron

import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import scala.concurrent.duration._
import com.draugrsoft.integration.helper.constants.Cron._
import java.util.Calendar
import com.draugrsoft.integration.helper.constants.TimeConstant.Month

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
    val fromYear = fromCal.get(Calendar.YEAR)
    
    val nextSec = getNextSec(cronSec,fromSec)
    val nextMinute = getNextMin(cronMin,fromMinute)
    val nextHour = getNextHour(cronHour,fromHour)
    val nextDomOpt = getNextDayOfMonth(cronDom,fromDayOfMonth) 
    val nextDowOpt = getNextDayOfWeek(cronDow,fromDayOfWeek) 
    
    val nextExecutionCal = Calendar.getInstance
    nextExecutionCal.set(Calendar.SECOND, nextSec)
    nextExecutionCal.set(Calendar.MINUTE, nextMinute)
    nextExecutionCal.set(Calendar.HOUR, nextHour)
    
    // Only one of the 2 below should be present
    nextDomOpt.foreach(nextDom => nextExecutionCal.set(Calendar.DAY_OF_MONTH,nextDom))
    nextDowOpt.foreach(nextDow => nextExecutionCal.set(Calendar.DAY_OF_WEEK, nextDow))
    
    
    
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
   * Is the second field going to roll over with the provided cron expression?
   * This is used to determine if the field above(minute) needs to advance
   */
  def isRolloverSec(cronSec:CronSeconds, fromSec:Int):Boolean = {
    getNextSec(cronSec,fromSec) < fromSec
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
  
    /**
   * Is the second field going to roll over with the provided cron expression?
   * This is used to determine if the field above(hour) needs to advance
   */
  def isRolloverMin(cronMin:CronMinutes, fromMinute:Int):Boolean = {
    getNextMin(cronMin,fromMinute) < fromMinute
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
  
    /**
   * Is the second field going to roll over with the provided cron expression?
   * This is used to determine if the field above(day) needs to advance
   */
  def isRolloverHour(cronHour:CronHours, fromHour:Int):Boolean = {
    getNextHour(cronHour,fromHour) < fromHour
  }
  
  def getNextDayOfMonth(cronDom:CronDayOfMonth, fromDayOfMonth:Int) :Option[Int] = {
    cronDom match{
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
        case NoVal => None
        case DayOfWeekVal(either) => Some{
          // Get the sorted Days of Weeks with the number representations
          val sortedDow = (either match {
            case Left(xs) => xs
            case Right(xs) => xs.map(_.numberRepresentation)
          }).sorted
          // User number representations to get the next day
          sortedDow.find(_ > fromDayOfWeek) match {
            case Some(dow) => dow
            case None => sortedDow.head
          }
        }
    }
  }
  
  /**
   * Is it the end of the month
   */
  def isRollOverDay(fromDayOfMonth:Int, month:Month, year:Int):Boolean = {
      val lastDayOfMonth = month.getLastDay(year)
      fromDayOfMonth == lastDayOfMonth  
  }  
}