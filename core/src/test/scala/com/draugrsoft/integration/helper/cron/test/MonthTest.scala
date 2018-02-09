package com.draugrsoft.integration.helper.cron.test

import org.scalatest.WordSpec
import org.scalatest.Matchers
import com.draugrsoft.integration.helper.constants.TimeConstant._

class MonthTest extends WordSpec with Matchers {

  "getgetLastDay" should {

    "return 29 for leap years" in {
      assertResult(29)(Feb.getLastDay(2020))
      assertResult(29)(Feb.getLastDay(2016))
    }

    "return 28 for non-leap years" in {
      assertResult(28)(Feb.getLastDay(1999))
      assertResult(28)(Feb.getLastDay(2001))
      assertResult(28)(Feb.getLastDay(2002))
      assertResult(28)(Feb.getLastDay(2003))
    }

  }

  "convertMonthNumberToMonth  " should {

    "throw an exception with a parameter greater than 12" in {
      assertThrows[IllegalArgumentException] {
        Month.convertMonthNumberToMonth(13)
      }
    }

    "throw an exception with a 0 paramter" in {
      assertThrows[IllegalArgumentException] {
        Month.convertMonthNumberToMonth(0)
      }
    }
    
    "throw an exception with a negative paramter" in {
      assertThrows[IllegalArgumentException] {
        Month.convertMonthNumberToMonth(-5)
      }
    }
    
    "return the correct month with 6 as input" in {
      assertResult(June)(Month.convertMonthNumberToMonth(6))
    }

  }
  
  "getNextMonth " should {
    
    "return Feb with Jan as input" in {
      assertResult(Feb)(Month.getNextMonth(Jan))
    }
    
    "return July with June as input" in {
      assertResult(July)(Month.getNextMonth(June))
    }
    
    "return Jan with Dec as input " in {
       assertResult(Jan)(Month.getNextMonth(Dec))
    }
  }
  
  "isEndOfYear " should {
    
    "return true when Dec is input " in {
      assertResult(true)(Month.isEndOfYear(Dec))
    }
    
    "return false when Nov is input " in {
      assertResult(false)(Month.isEndOfYear(Nov))
    }
    
     "return false when Jan is input " in {
      assertResult(false)(Month.isEndOfYear(Jan))
    }
  }
  
  
  
  /*
  def getNextMonth(month: Month): Month = {
      if(month.num == Dec.num){
        Jan 
      }else {
        convertMonthNumberToMonth(month.num + 1)
      }
    }
    
    def isEndOfYear(month:Month):Boolean = {
      month.num == Dec.num
    }
  
	*/
}