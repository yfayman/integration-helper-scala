package com.draugrsoft.integration.helper.cron.test

import org.scalatest.WordSpec
import org.scalatest.Matchers
import com.draugrsoft.integration.helper.cron.NextRunCalculator
import java.util.Calendar
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import scala.concurrent.duration._
import com.draugrsoft.integration.helper.constants.Cron._

class NextRunCalculatorTest extends WordSpec with Matchers {

  "NextRunCalculatorTest" should {

    // 15th of January, 2018
    val twoPM = 1516042800000l //starting time for all tests
    val fourPM = 1516050000000l
    val fivePM = 1516053600000l
    val sixThirtyPM = 1516059000000l

    val cal = Calendar.getInstance
    cal.setTimeInMillis(twoPM) // 1/15/2018 2PM ESTs

    // 16th of January, 2018
    val twelvePM = 1516122000000l

    "produce accurate delay for a ScheduleSingleTrigger" in {               
      val resultOne = NextRunCalculator(twoPM, ScheduleSingleTrigger(1516042900000l)) // changed 8th number 8 -> 9
      assertResult(100000.millis)(resultOne)

      val resultTwo = NextRunCalculator(twoPM, ScheduleSingleTrigger(1516042850000l)) // changed 9th number 0 -> 5
      assertResult(50000.millis)(resultTwo)
    }

    "produce accurate delay for ScheduleSecondTrigger" in {
      val resultOne = NextRunCalculator(twoPM, ScheduleSecondTrigger(20))
      assertResult(20000.millis)(resultOne)

      val resultTwo = NextRunCalculator(twoPM, ScheduleSecondTrigger(10))
      assertResult(10000.millis)(resultTwo)
    }
//TODO Below
    "produce accurate delay for ScheduleCronTrigger with a hour based config" in {
      val fivePMTrigger = ScheduleCronTrigger(
        seconds = new SecondsVal(0),
        minutes = new MinutesVal(0),
        hours = new HoursVal(5),
        dayOfMonth = All,
        dayOfWeek = All,
        month = All)
      val result = NextRunCalculator(twoPM, fivePMTrigger)
      assertResult((fivePM - twoPM).millis)(result)

      val fourPMFivePMTrigger = ScheduleCronTrigger(
        seconds = new SecondsVal(0),
        minutes = new MinutesVal(0),
        hours = HoursVal(4 :: 5 :: Nil),
        dayOfMonth = All,
        dayOfWeek = All,
        month = All)
      val resultTwo = NextRunCalculator(twoPM, fourPMFivePMTrigger)
      assertResult((fourPM - twoPM).millis)(resultTwo)
      
      
      val sixThirtyPMTrigger = ScheduleCronTrigger(
        seconds = new SecondsVal(0),
        minutes = new MinutesVal(0),
        hours = HoursVal(4 :: 5 :: Nil),
        dayOfMonth = All,
        dayOfWeek = All,
        month = All)
      val resultThree = NextRunCalculator(twoPM, sixThirtyPMTrigger)
      assertResult((sixThirtyPM - twoPM).millis)(resultThree)

    }
 //TODO   Above
    "produce the correct next seconds value" in {
      
      // If there is no next item on the list
      val nextSecListRollOver = NextRunCalculator.getNextSec(SecondsVal(List(5,25,45)), 55)
      assertResult(5)(nextSecListRollOver)
      
      val nextSecList = NextRunCalculator.getNextSec(SecondsVal(List(5,25,45)), 20)
      assertResult(25)(nextSecList)
      
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
      val nextMinListRollOver = NextRunCalculator.getNextMin(MinutesVal(List(5,25,45)), 55)
      assertResult(5)(nextMinListRollOver)
      
      val nextMinList = NextRunCalculator.getNextMin(MinutesVal(List(5,25,45)), 20)
      assertResult(25)(nextMinList)
      
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
    
    "produce the correct next hours value "  in {
      // If there is no next item on the list
      val nextHourListRollOver = NextRunCalculator.getNextHour(HoursVal(List(4,8,12)), 13)
      assertResult(4)(nextHourListRollOver)
      
      val nextHourList = NextRunCalculator.getNextHour(HoursVal(List(4,8,12)), 6)
      assertResult(8)(nextHourList)
      
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
      val nextDayOfMonthListRollOver = NextRunCalculator.getNextDayOfMonth(DayOfMonthVal(List(1,5,10)), 24)
      assertResult(Some(1))(nextDayOfMonthListRollOver)
      
      val nextDayOfMonthlist = NextRunCalculator.getNextDayOfMonth(DayOfMonthVal(List(1,5,10)), 3)
      assertResult(Some(5))(nextDayOfMonthlist)
      
      // With a wildcard, the next day of month should be returned
      val nextDayOfMonthAll = NextRunCalculator.getNextDayOfMonth(All, 24)
      assertResult(Some(25))(nextDayOfMonthAll)
      
      // With one cron value, it should always return that value
      val nextValSingle = NextRunCalculator.getNextDayOfMonth(new DayOfMonthVal(25), 29)
      assertResult(Some(25))(nextValSingle)
      
      // 31 should go to 1
      val nextHourAllRollOver = NextRunCalculator.getNextDayOfMonth(All, 31);
      assertResult(Some(1))(nextHourAllRollOver)
      
      // If input is NoVal, output should be None
      val nextHourNoVal = NextRunCalculator.getNextDayOfMonth(NoVal, 3)
      assertResult(None)(nextHourNoVal)
    }
  }

}