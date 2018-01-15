package com.draugrsoft.integration.helper.constants

import TimeConstant._

object Cron {
  
  
  sealed trait CronSeconds
  sealed trait CronMinutes
  sealed trait CronHours
  sealed trait CronDayOfMonth
  sealed trait CronDayOfWeek
  sealed trait CronMonth
  
  
  case object All extends CronSeconds with CronMinutes with CronHours with CronDayOfMonth
                  with CronMonth with CronDayOfWeek
                  
  case object NoVal extends CronSeconds with CronMinutes with CronHours with CronDayOfMonth
                    with CronMonth with CronDayOfWeek
  
  case class SecondsVal(seconds:List[Int]) extends CronSeconds{
    def this(seconds:Int) = this(seconds::Nil)
    assert(seconds.forall(s => s >= 0 && s < 60 ))
  }
  
  case class MinutesVal(minutes:List[Int]) extends CronMinutes {
    def this(minutes:Int) = this(minutes :: Nil)
    assert(minutes.forall(m => m >= 0 && m < 60 ))
  }
  
  case class HoursVal(hours:List[Int]) extends CronHours{
    def this(hours:Int) = this(hours::Nil)
    assert(hours.forall(h => h>=0 && h < 24))
  }
  
  case class DayOfMonthVal(daysOfMonth:List[Int]) extends CronDayOfMonth{
    def this(dayOfMonth:Int) = this(dayOfMonth::Nil)
    assert(daysOfMonth.forall(dom => dom > 0 && dom <= 31 ))
  }
  
  case class MonthVal(either:Either[List[Int],List[Month]]) extends CronMonth{
    assert {
        either match{
        case Left(xsi) => xsi.forall(monthInt => monthInt > 0 && monthInt <= 12)
        case Right(xsm) => true 
      }
    }
  }
  
  case class DayOfWeekVal(either:Either[List[Int],List[DayOfWeek]]) extends CronDayOfWeek{
    assert{
      either match{
        case Left(xsi) => xsi.forall(dayInt => dayInt > 0 && dayInt <= 7)
        case Right(xsd) => true
      }
    }
  }
  
  
  
  
  
}