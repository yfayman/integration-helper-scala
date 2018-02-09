package com.draugrsoft.integration.helper.constants

private[integration] object TimeConstant {

  trait ThiryOneDayMonth {
    def getLastDay(year:Int) = {
      assert(year > 0)
      31
    }
  }
  
  trait ThirtyDayMonth{
    def getLastDay(year:Int) = {
      assert(year > 0)
      30
    }
  }
  
  sealed abstract class Month{
    def getLastDay(year:Int):Int
  }
  case object Jan extends Month with ThiryOneDayMonth
  case object Feb extends Month {
    def getLastDay(year:Int) = {
      assert(year > 0)
      if(year % 4 == 0){
        29
      }else{
        28
      }
    }
  }
  case object Mar extends Month with ThiryOneDayMonth
  case object April extends Month with ThirtyDayMonth
  case object May extends Month with ThiryOneDayMonth
  case object June extends Month with ThirtyDayMonth
  case object July extends Month with ThiryOneDayMonth
  case object Aug extends Month with ThiryOneDayMonth
  case object Sept extends Month with ThirtyDayMonth
  case object Oct extends Month with ThiryOneDayMonth
  case object Nov extends Month with ThirtyDayMonth
  case object Dec extends Month with ThiryOneDayMonth
  
  sealed abstract class DayOfWeek(val numberRepresentation:Int)
  case object Sun extends DayOfWeek(1) // 1
  case object Mon extends DayOfWeek(2) // 2
  case object Tues extends DayOfWeek(3) // 3
  case object Wed extends DayOfWeek(4) // 4
  case object Thur extends DayOfWeek(5) // 5
  case object Fri extends DayOfWeek(6) // 6
  case object Sat extends DayOfWeek(7) // 7
  

}