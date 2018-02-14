package com.draugrsoft.integration.helper.cron

import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import scala.concurrent.duration._
import com.draugrsoft.integration.helper.constants.Cron._
import java.util.Calendar
import com.draugrsoft.integration.helper.constants.TimeConstant._
import java.util.TimeZone
import java.util.GregorianCalendar
import com.draugrsoft.integration.helper.constants.TimeConstant

object NextRunCalculator {

  val gmt = TimeZone.getTimeZone("GMT")

  def apply(sched: Schedule): FiniteDuration = apply(System.currentTimeMillis(), sched)

  def apply(from: Long, sched: Schedule): FiniteDuration = {
    sched match {
      case ScheduleSingleTrigger(fireTime)                    => (fireTime - from).millis
      case ScheduleSecondTrigger(sec)                         => sec.seconds
      case ScheduleCronTrigger(sec, min, hour, dom, dow, mon) => calculateTimeToNextTrigger(from, sec, min, hour, dom, dow, mon)
    }
  }

  protected def calculateTimeToNextTrigger(
    fromTime: Long,
    cronSec:  CronSeconds,
    cronMin:  CronMinutes,
    cronHour: CronHours,
    cronDom:  CronDayOfMonth,
    cronDow:  CronDayOfWeek,
    cronMon:  CronMonth): FiniteDuration = {

    // check to make sure both are not selected
    assert(cronDom.isOmmitted || cronDow.isOmmitted)

    val fromCal = Calendar.getInstance(gmt);
    fromCal.setTimeInMillis(fromTime)

    val fromMilli = fromCal.get(Calendar.MILLISECOND)
    val fromSec = fromCal.get(Calendar.SECOND)
    val fromMinute = fromCal.get(Calendar.MINUTE)
    val fromHour = fromCal.get(Calendar.HOUR_OF_DAY)
    val fromDayOfMonth = fromCal.get(Calendar.DAY_OF_MONTH)
    val fromDayOfWeek = fromCal.get(Calendar.DAY_OF_WEEK)
    val fromMonthNum = fromCal.get(Calendar.MONTH) + 1 // Calendar goes 0 to 11. We need 1 to 12
    val fromMonth = Month.convertMonthNumberToMonth(fromMonthNum)
    val fromYear = fromCal.get(Calendar.YEAR)

    val nextSec = getNextSec(cronSec, fromSec)

    val nextMinute = getNextMin(cronMin, fromMinute, cronSec, fromSec)
    val nextHour = getNextHour(cronHour, fromHour, cronMin, fromMinute)
    val nextDate = getNextDate(cronDom, cronDow, cronHour, fromHour, fromDayOfMonth, fromDayOfWeek, fromYear, fromMonth)
    val nextMonth = getNextMonth(nextHour, fromMonthNum, fromDayOfMonth, fromYear)
    val nextYear = {
      if (Month.isEndOfYear(nextMonth)) {
        fromYear + 1
      } else {
        fromYear
      }
    }

    val nextExecutionCal = Calendar.getInstance(gmt)
    nextExecutionCal.set(Calendar.MILLISECOND, fromMilli)
    nextExecutionCal.set(Calendar.SECOND, nextSec)
    nextExecutionCal.set(Calendar.MINUTE, nextMinute)
    nextExecutionCal.set(Calendar.HOUR_OF_DAY, nextHour)
    nextExecutionCal.set(Calendar.DAY_OF_MONTH, nextDate)
    nextExecutionCal.set(Calendar.MONTH, nextMonth.num - 1) //Calendar uses 0 - 11
    nextExecutionCal.set(Calendar.YEAR, nextYear)

    val toTime = nextExecutionCal.getTimeInMillis

    FiniteDuration(toTime - fromTime, MILLISECONDS)
  }

  /**
   * Calculates the next second component of the next fire.
   * Ex. If a job fires at 20,40,50 seconds and the fromSec is 30,
   * then the next one should be 40
   */
  def getNextSec(cronSec: CronSeconds, fromSec: Int): Int = {

    def getNextSecFromExpr(sv: List[Int], fromSec: Int, inclusive: Boolean): Int = {
      val sortedSeconds = sv.sorted
      val nextValOpt = {
        if (inclusive) {
          sortedSeconds.find(_ >= fromSec)
        } else {
          sortedSeconds.find(_ > fromSec)
        }
      }
      nextValOpt.find(_ > fromSec) match {
        case Some(sec) => sec
        case None      => sortedSeconds.head
      }
    }

    cronSec match {
      case All            => if (fromSec == 59) 0 else fromSec + 1
      case SecondsVal(xs) => getNextSecFromExpr(xs, fromSec, false)
    }
  }

  /**
   * Does the second roll over
   */
  private[cron] def isRolloverSec(cronSec: CronSeconds, fromSec: Int): Boolean = {
    cronSec match {
      case All => fromSec == 59
      case SecondsVal(xs) => {
        xs.sorted.find(_ > fromSec).isEmpty
      }
    }
  }

  /**
   * Calculates the next minute component of the next fire.
   * Ex. If a job fires at 20,40,50 seconds and the fromMinute is 30,
   * then the next one should be 40. This also depends on the Seconds part of the expression
   */
  def getNextMin(cronMin: CronMinutes, fromMinute: Int,
                 cronSec: CronSeconds, fromSec: Int): Int = {

    def getNextMinuteFromExpr(mv: List[Int], fromMinute: Int, inclusive: Boolean): Int = {
      val sortedMintues = mv.sorted
      val nextValOpt = {
        if (inclusive) {
          sortedMintues.find(_ >= fromMinute)
        } else {
          sortedMintues.find(_ > fromMinute)
        }
      }
      nextValOpt match {
        case Some(min) => min
        case None      => sortedMintues.head
      }
    }
    if (isRolloverSec(cronSec, fromSec)) {
      cronMin match {
        case All            => if (fromMinute == 59) 0 else fromMinute + 1
        case MinutesVal(xs) => getNextMinuteFromExpr(xs, fromMinute, false)
      }
    } else {
      cronMin match {
        case All            => fromMinute
        case MinutesVal(xs) => getNextMinuteFromExpr(xs, fromMinute, true)
      }
    }

  }

  private[cron] def isRolloverMin(cronMin: CronMinutes, fromMin: Int): Boolean = {
    cronMin match {
      case All            => fromMin == 59
      case MinutesVal(xs) => xs.sorted.find(_ > fromMin).isEmpty

    }
  }

  /**
   * 0-23 represent the range of values that
   * can return from this function
   */
  def getNextHour(cronHour: CronHours, fromHour: Int,
                  cronMin: CronMinutes, fromMinute: Int): Int = {

    def getNextHourFromExpr(hv: List[Int], fromHour: Int, inclusive: Boolean): Int = {
      val sortedHours = hv.sorted
      val nextValOpt = {
        if (inclusive) {
          sortedHours.find(_ >= fromHour)
        } else {
          sortedHours.find(_ > fromHour)
        }
      }
      nextValOpt match {
        case Some(hour) => hour
        case None       => sortedHours.head
      }
    }

    if (isRolloverMin(cronMin, fromMinute)) {
      cronHour match {
        case All          => if (fromHour == 23) 0 else fromHour + 1
        case HoursVal(xs) => getNextHourFromExpr(xs, fromHour, false)
      }
    } else {
      cronHour match {
        case All          => fromHour
        case HoursVal(xs) => getNextHourFromExpr(xs, fromHour, true)
      }
    }

  }

  /**
   * Is the second field going to roll over with the provided cron expression?
   * This is used to determine if the field above(day) needs to advance
   */
  private[cron] def isRolloverHour(cronHour: CronHours, fromHour: Int): Boolean = {
    cronHour match {
      case All          => fromHour == 23
      case HoursVal(xs) => xs.sorted.find(_ > fromHour).isEmpty
    }
  }

  /**
   * Returns the next DoM. 1 - 31 represent the possible return values
   */
  def getNextDayOfMonth(cronDom: CronDayOfMonth, fromDayOfMonth: Int,
                        cronHour: CronHours, fromHour: Int,
                        month: Month, year: Int): Option[Int] = {

    def getNextMonthFromExpr(mv: List[Int], fromDayOfMonth: Int): Option[Int] = {
      val sortedDoms = mv.sorted
      Option {
        sortedDoms.find(_ > fromDayOfMonth) match {
          case Some(dom) => dom
          case None      => sortedDoms.head
        }
      }
    }

    val lastDayofMonth = month.getLastDay(year)

    if (isRolloverHour(cronHour, fromHour)) {
      cronDom match {
        case NoVal             => None
        case DayOfMonthVal(xs) => getNextMonthFromExpr(xs, fromDayOfMonth)
      }
    } else {
      cronDom match {
        case NoVal             => None
        case DayOfMonthVal(xs) => getNextMonthFromExpr(xs, fromDayOfMonth)
      }
    }
  }

  def getNextDayOfWeek(cronDow: CronDayOfWeek, fromDayOfWeek: Int,
                       cronHour: CronHours, fromHour: Int): Option[Int] = {

    def getNextDayOfWeekFromExpr(either: Either[List[Int], List[DayOfWeek]], fromDayOfWeek: Int): Option[Int] = {
      Some {
        // Get the sorted Days of Weeks with the number representations
        val sortedDow = (either match {
          case Left(xs)  => xs
          case Right(xs) => xs.map(_.numberRepresentation)
        }).sorted
        // User number representations to get the next day
        sortedDow.find(_ > fromDayOfWeek) match {
          case Some(dow) => dow
          case None      => sortedDow.head
        }
      }
    }

    if (isRolloverHour(cronHour, fromHour)) {
      cronDow match {
        case NoVal                => None
        case DayOfWeekVal(either) => getNextDayOfWeekFromExpr(either, fromDayOfWeek)
      }
    } else {
      cronDow match {
        case NoVal => None
        case DayOfWeekVal(either) => Some {
          // Get the sorted Days of Weeks with the number representations
          val sortedDow = (either match {
            case Left(xs)  => xs
            case Right(xs) => xs.map(_.numberRepresentation)
          }).sorted
          // User number representations to get the next day
          sortedDow.find(_ > fromDayOfWeek) match {
            case Some(dow) => dow
            case None      => sortedDow.head
          }
        }
        case _ => Some { if (fromDayOfWeek == 7) 1 else fromDayOfWeek + 1 }
      }
    }

  }

  /**
   * Is it the end of the month
   */
  private[cron] def isRollOverDay(fromDayOfMonth: Int, month: Month, year: Int): Boolean = {
    val lastDayOfMonth = month.getLastDay(year)
    fromDayOfMonth == lastDayOfMonth
  }

  def getNextMonth(nextHour: Int, fromMonthNum: Int, fromDayOfMonth: Int, fromYear: Int): Month = {
    val fromMonth = Month.convertMonthNumberToMonth(fromMonthNum)
    val isEndOfMonth = isRollOverDay(fromDayOfMonth, fromMonth, fromYear)

    if (isEndOfMonth && nextHour == 0) {
      Month.getNextMonth(fromMonth)
    } else {
      Month.convertMonthNumberToMonth(fromMonthNum)
    }
  }

  def getNextDate(
    cronDom:        CronDayOfMonth,
    cronDow:        CronDayOfWeek,
    cronHour:       CronHours,
    fromHour:       Int,
    fromDayOfMonth: Int,
    fromDayOfWeek:  Int,
    fromYear:       Int,
    fromMonth:      Month): Int = {

    val lastDayOfMonth = fromMonth.getLastDay(fromYear)

    if (!cronDom.isOmmitted) {
      val nextDom = getNextDayOfMonth(cronDom, fromDayOfMonth, cronHour, fromHour, fromMonth, fromYear).get
      if (nextDom > lastDayOfMonth) 1 else nextDom
    } else if (!cronDow.isOmmitted) {
      val nextDow = getNextDayOfWeek(cronDow, fromDayOfWeek, cronHour, fromHour).get

      //Need to calculate the distance between the days and then add it to figure out the next date
      val futureDate = if (nextDow > fromDayOfWeek) {
        val daysApart = nextDow - fromDayOfWeek
        fromDayOfMonth + daysApart
      } else {
        val daysApart = fromDayOfWeek - nextDow
        fromDayOfMonth + (7 - daysApart)
      }

      if (futureDate > lastDayOfMonth) {
        futureDate - lastDayOfMonth // next month
      } else {
        futureDate
      }

    } else {
      if (isRolloverHour(cronHour, fromHour)) {
        fromDayOfMonth + 1
      } else {
        fromDayOfMonth
      }
    }
  }
}