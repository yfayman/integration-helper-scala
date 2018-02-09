package com.draugrsoft.integration.helper.cron.test

import org.scalatest.WordSpec
import org.scalatest.Matchers
import com.draugrsoft.integration.helper.cron.NextRunCalculator
import java.util.Calendar
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import scala.concurrent.duration._
import com.draugrsoft.integration.helper.constants.Cron._
import com.draugrsoft.integration.helper.constants.TimeConstant._

class NextRunCalculatorTest extends WordSpec with Matchers {

  "NextRunCalculatorTest" should {

    // 15th of January, 2018
    val twoPM = 1516024800000l //starting time for all tests
    val fourPM = 1516032000000l
    val fivePM = 1516035600000l
    val sixThirtyPM = 1516041000000l

    val cal = Calendar.getInstance
    cal.setTimeInMillis(twoPM) // 1/15/2018 2PM ESTs

    // 16th of January, 2018
    val twelvePM = 1516122000000l

    "produce accurate delay for a ScheduleSingleTrigger" in {
      val resultOne = NextRunCalculator(twoPM, ScheduleSingleTrigger(1516024900000l)) // changed 8th number 8 -> 9
      assertResult(100000.millis)(resultOne)

      val resultTwo = NextRunCalculator(twoPM, ScheduleSingleTrigger(1516024850000l)) // changed 9th number 0 -> 5
      assertResult(50000.millis)(resultTwo)
    }

    "produce accurate delay for ScheduleSecondTrigger" in {
      val resultOne = NextRunCalculator(twoPM, ScheduleSecondTrigger(20))
      assertResult(20000.millis)(resultOne)

      val resultTwo = NextRunCalculator(twoPM, ScheduleSecondTrigger(10))
      assertResult(10000.millis)(resultTwo)
    }
    
    "isRolloverSec method should return correctly" in {
      assertResult(true)(NextRunCalculator.isRolloverSec(All, 59)) 
      assertResult(false)(NextRunCalculator.isRolloverSec(All, 55))
      
      // with cron data
      assertResult(true)(NextRunCalculator.isRolloverSec(SecondsVal(List(5, 25, 45)), 46)) 
      assertResult(true)(NextRunCalculator.isRolloverSec(SecondsVal(List(5, 25, 45)), 45)) 
      assertResult(false)(NextRunCalculator.isRolloverSec(SecondsVal(List(5, 25, 45)), 27)) 
    }
    
    "isRolloverMin should return correctly" in {
      assertResult(true)(NextRunCalculator.isRolloverMin(All, 59))  
      assertResult(false)(NextRunCalculator.isRolloverMin(All, 55))  
      
       // with cron data
      assertResult(true)(NextRunCalculator.isRolloverMin(MinutesVal(List(5, 25, 45)), 46)) 
      assertResult(true)(NextRunCalculator.isRolloverMin(MinutesVal(List(5, 25, 45)), 45)) 
      assertResult(false)(NextRunCalculator.isRolloverMin(MinutesVal(List(5, 25, 45)), 27)) 
    }
    
    "is RolloverHour should return correctly" in {
      assertResult(true)(NextRunCalculator.isRolloverHour(All, 23))  
      assertResult(false)(NextRunCalculator.isRolloverHour(All, 20))  
      
      // with cron data
      assertResult(true)(NextRunCalculator.isRolloverHour(HoursVal(List(2, 4, 15)), 20)) 
      assertResult(true)(NextRunCalculator.isRolloverHour(HoursVal(List(2, 4, 15)), 15)) 
      assertResult(false)(NextRunCalculator.isRolloverHour(HoursVal(List(2, 4, 15)), 10))
    }
    
    //TODO Below
    "produce accurate delay for ScheduleCronTrigger with a hour based config" in {
      val fivePMTrigger = ScheduleCronTrigger(
        seconds = new SecondsVal(0),
        minutes = new MinutesVal(0),
        hours = new HoursVal(17),
        dayOfMonth = NoVal,
        dayOfWeek = NoVal,
        month = All)
      val result = NextRunCalculator(twoPM, fivePMTrigger)
      assertResult((fivePM - twoPM).millis)(result)

      val fourPMFivePMTrigger = ScheduleCronTrigger(
        seconds = new SecondsVal(0),
        minutes = new MinutesVal(0),
        hours = HoursVal(16 :: 17 :: Nil),
        dayOfMonth = NoVal,
        dayOfWeek = NoVal,
        month = All)
      val resultTwo = NextRunCalculator(twoPM, fourPMFivePMTrigger)
      assertResult((fourPM - twoPM).millis)(resultTwo)

      val sixThirtyPMTrigger = ScheduleCronTrigger(
        seconds = new SecondsVal(0),
        minutes = new MinutesVal(0),
        hours = HoursVal(16 :: 17 :: Nil),
        dayOfMonth = NoVal,
        dayOfWeek = NoVal,
        month = All)
      val resultThree = NextRunCalculator(twoPM, sixThirtyPMTrigger)
      assertResult((sixThirtyPM - twoPM).millis)(resultThree)

    }
    //TODO   Above
    "produce the correct next seconds value" in {

      // If there is no next item on the list
      val nextSecListRollOver = NextRunCalculator.getNextSec(SecondsVal(List(5, 25, 45)), 55)
      assertResult(5)(nextSecListRollOver)

      // Current time is equal to the last in the list
      val nextSecListRollOverStartOnTrig = NextRunCalculator.getNextSec(SecondsVal(List(5, 25, 45)), 45)
      assertResult(5)(nextSecListRollOverStartOnTrig)

      val nextSecList = NextRunCalculator.getNextSec(SecondsVal(List(5, 25, 45)), 20)
      assertResult(25)(nextSecList)

      val nextSecListStartOnTrig = NextRunCalculator.getNextSec(SecondsVal(List(5, 25, 45)), 5)
      assertResult(25)(nextSecListStartOnTrig)

      // With a wildcard, the next second should be returned
      val nextSecAll = NextRunCalculator.getNextSec(All, 24)
      assertResult(25)(nextSecAll)

      // With one cron value, it should always return that value
      val nextValSingle = NextRunCalculator.getNextSec(new SecondsVal(39), 58)
      assertResult(39)(nextValSingle)

      // 59 should go to 0
      val nextSecAllRollOver = NextRunCalculator.getNextSec(All, 59)
      assertResult(0)(nextSecAllRollOver)
    }

    "produce the correct next minutes value" in {

      // If there is no next item on the list
      val nextMinListRollOver = NextRunCalculator.getNextMin(MinutesVal(List(5, 25, 45)), 55)
      assertResult(5)(nextMinListRollOver)

      // If there is no next item on the list. Start on last value
      val nextMinListRollOverStartOnTrigl = NextRunCalculator.getNextMin(MinutesVal(List(5, 25, 45)), 45)
      assertResult(5)(nextMinListRollOverStartOnTrigl)

      val nextMinList = NextRunCalculator.getNextMin(MinutesVal(List(5, 25, 45)), 20)
      assertResult(25)(nextMinList)

      val nextMinListStartOnTrig = NextRunCalculator.getNextMin(MinutesVal(List(5, 25, 45)), 5)
      assertResult(25)(nextMinListStartOnTrig)

      // With a wildcard, the next minute should be returned
      val nextMinAll = NextRunCalculator.getNextMin(All, 24)
      assertResult(25)(nextMinAll)

      // With one cron value, it should always return that value
      val nextValSingle = NextRunCalculator.getNextMin(new MinutesVal(39), 58)
      assertResult(39)(nextValSingle)

      // 59 should go to 0
      val nextMinAllRollOver = NextRunCalculator.getNextMin(All, 59);
      assertResult(0)(nextMinAllRollOver)

    }

    "produce the correct next hours value " in {
      // If there is no next item on the list
      val nextHourListRollOver = NextRunCalculator.getNextHour(HoursVal(List(4, 8, 12)), 13)
      assertResult(4)(nextHourListRollOver)

      // If there is no next item on the list. Starting on last trigger val
      val nextHourListRollOverStartOnTrig = NextRunCalculator.getNextHour(HoursVal(List(4, 8, 12)), 12)
      assertResult(4)(nextHourListRollOverStartOnTrig)

      val nextHourList = NextRunCalculator.getNextHour(HoursVal(List(4, 8, 12)), 6)
      assertResult(8)(nextHourList)

      val nextHourListStartOnTrig = NextRunCalculator.getNextHour(HoursVal(List(4, 8, 12)), 4)
      assertResult(8)(nextHourListStartOnTrig)

      // With a wildcard, the next hour should be returned
      val nextHourAll = NextRunCalculator.getNextHour(All, 22)
      assertResult(23)(nextHourAll)

      // With one cron value, it should always return that value
      val nextValSingle = NextRunCalculator.getNextHour(new HoursVal(12), 14)
      assertResult(12)(nextValSingle)

      // 23 should go to 0
      val nextHourAllRollOver = NextRunCalculator.getNextHour(All, 23);
      assertResult(0)(nextHourAllRollOver)
    }

    "produce the correct next Day of Month value" in {

      // If there is no next item on the list
      val nextDayOfMonthListRollOver = NextRunCalculator.getNextDayOfMonth(DayOfMonthVal(List(1, 5, 10)), 11)
      assertResult(Some(1))(nextDayOfMonthListRollOver)

      // Start on the last item
      val nextDayOfMonthListRollOverStartOnTrig = NextRunCalculator.getNextDayOfMonth(DayOfMonthVal(List(1, 5, 10)), 10)
      assertResult(Some(1))(nextDayOfMonthListRollOverStartOnTrig)

      val nextDayOfMonthlist = NextRunCalculator.getNextDayOfMonth(DayOfMonthVal(List(1, 5, 10)), 3)
      assertResult(Some(5))(nextDayOfMonthlist)

      val nextDayOfMonthlistStartOnTrig = NextRunCalculator.getNextDayOfMonth(DayOfMonthVal(List(1, 5, 10)), 1)
      assertResult(Some(5))(nextDayOfMonthlistStartOnTrig)

      // With one cron value, it should always return that value
      val nextValSingle = NextRunCalculator.getNextDayOfMonth(new DayOfMonthVal(25), 29)
      assertResult(Some(25))(nextValSingle)

      // If input is NoVal, output should be None
      val nextHourNoVal = NextRunCalculator.getNextDayOfMonth(NoVal, 3)
      assertResult(None)(nextHourNoVal)
    }

    "produce the correct next Day of Week value" in {

      val nextDayofWeekLeftListRollOver = NextRunCalculator.getNextDayOfWeek(DayOfWeekVal(Left(List(2, 3, 4))), 5)
      assertResult(Some(2))(nextDayofWeekLeftListRollOver)

      val nextDayofWeekLeftListRollOverStartOnTrig = NextRunCalculator.getNextDayOfWeek(DayOfWeekVal(Left(List(2, 3, 4))), 4)
      assertResult(Some(2))(nextDayofWeekLeftListRollOverStartOnTrig)

      val nextDayofWeekRightListRollOver = NextRunCalculator.getNextDayOfWeek(DayOfWeekVal(Right(List(Sun, Mon, Tues, Wed))), 5)
      assertResult(Some(1))(nextDayofWeekRightListRollOver)

      val nextDayofWeekRightListRollOverStartOnTrig = NextRunCalculator.getNextDayOfWeek(DayOfWeekVal(Right(List(Sun, Mon, Tues, Wed))), 4)
      assertResult(Some(1))(nextDayofWeekRightListRollOverStartOnTrig)

      val nextDayOfWeekLeftlist = NextRunCalculator.getNextDayOfWeek(DayOfWeekVal(Left(List(2, 3, 6))), 4)
      assertResult(Some(6))(nextDayOfWeekLeftlist)

      val nextDayOfWeekLeftlistStartOnTrig = NextRunCalculator.getNextDayOfWeek(DayOfWeekVal(Left(List(2, 3, 6))), 3)
      assertResult(Some(6))(nextDayOfWeekLeftlistStartOnTrig)
      
      val nextDayOfWeekNoVal = NextRunCalculator.getNextDayOfWeek(NoVal, 5)
      assertResult(None)(nextDayOfWeekNoVal)
    }
    
    "isRollOverDay should produce the correct answer on whether month needs to be incremented" in {
      assertResult(true)(NextRunCalculator.isRollOverDay(31, Jan, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, Jan, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, Jan, 2018))
      
      assertResult(true)(NextRunCalculator.isRollOverDay(28, Feb, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, Feb, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, Feb, 2018))
      
      // Leap year
      assertResult(true)(NextRunCalculator.isRollOverDay(29, Feb, 2016))
      assertResult(false)(NextRunCalculator.isRollOverDay(28, Feb, 2016))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, Feb, 2016))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, Feb, 2016))
      
      assertResult(true)(NextRunCalculator.isRollOverDay(31, Mar, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, Mar, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, Mar, 2018))
      
      assertResult(true)(NextRunCalculator.isRollOverDay(30, April, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, April, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, April, 2018))
      
      assertResult(true)(NextRunCalculator.isRollOverDay(31, May, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, May, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, May, 2018))
      
      
      assertResult(true)(NextRunCalculator.isRollOverDay(30, June, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, June, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, June, 2018))
      
      assertResult(true)(NextRunCalculator.isRollOverDay(31, July, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, July, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, July, 2018))
      
      assertResult(true)(NextRunCalculator.isRollOverDay(31, Aug, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, Aug, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, Aug, 2018))
      
      assertResult(true)(NextRunCalculator.isRollOverDay(30, Sept, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, Sept, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, Sept, 2018))    
      
      assertResult(true)(NextRunCalculator.isRollOverDay(31, Oct, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, Oct, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, Oct, 2018))
      
      assertResult(true)(NextRunCalculator.isRollOverDay(30, Nov, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, Nov, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, Nov, 2018)) 
      
      assertResult(true)(NextRunCalculator.isRollOverDay(31, Dec, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(15, Dec, 2018))
      assertResult(false)(NextRunCalculator.isRollOverDay(1, Dec, 2018))
      
    }
    
    
  }

}