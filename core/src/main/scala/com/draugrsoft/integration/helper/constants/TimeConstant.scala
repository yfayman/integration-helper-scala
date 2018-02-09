package com.draugrsoft.integration.helper.constants

import scala.annotation.switch

private[integration] object TimeConstant {

  trait ThiryOneDayMonth {
    def getLastDay(year: Int) = {
      assert(year > 0)
      31
    }
  }

  trait ThirtyDayMonth {
    def getLastDay(year: Int) = {
      assert(year > 0)
      30
    }
  }

  object Month {

    def convertMonthNumberToMonth(m: Int): Month = {
      (m: @switch) match {
        case 1  => Jan
        case 2  => Feb
        case 3  => Mar
        case 4  => April
        case 5  => May
        case 6  => June
        case 7  => July
        case 8  => Aug
        case 9  => Sept
        case 10 => Oct
        case 11 => Nov
        case 12 => Dec
        case _  => throw new IllegalArgumentException
      }
    }

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
  }

  sealed abstract class Month(val num: Int) {
    def getLastDay(year: Int): Int

  }
  case object Jan extends Month(1) with ThiryOneDayMonth
  case object Feb extends Month(2) {
    def getLastDay(year: Int) = {
      assert(year > 0)
      if (year % 4 == 0) {
        29
      } else {
        28
      }
    }
  }
  case object Mar extends Month(3) with ThiryOneDayMonth
  case object April extends Month(4) with ThirtyDayMonth
  case object May extends Month(5) with ThiryOneDayMonth
  case object June extends Month(6) with ThirtyDayMonth
  case object July extends Month(7) with ThiryOneDayMonth
  case object Aug extends Month(8) with ThiryOneDayMonth
  case object Sept extends Month(9) with ThirtyDayMonth
  case object Oct extends Month(10) with ThiryOneDayMonth
  case object Nov extends Month(11) with ThirtyDayMonth
  case object Dec extends Month(12) with ThiryOneDayMonth

  sealed abstract class DayOfWeek(val numberRepresentation: Int)
  case object Sun extends DayOfWeek(1) // 1
  case object Mon extends DayOfWeek(2) // 2
  case object Tues extends DayOfWeek(3) // 3
  case object Wed extends DayOfWeek(4) // 4
  case object Thur extends DayOfWeek(5) // 5
  case object Fri extends DayOfWeek(6) // 6
  case object Sat extends DayOfWeek(7) // 7

}