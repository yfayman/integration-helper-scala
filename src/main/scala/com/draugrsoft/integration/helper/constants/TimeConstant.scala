package com.draugrsoft.integration.helper.constants

private[integration] object TimeConstant {

  sealed trait Month
  case object Jan extends Month
  case object Feb extends Month
  case object Mar extends Month
  case object May extends Month
  case object June extends Month
  case object July extends Month
  case object Aug extends Month
  case object Sept extends Month
  case object Oct extends Month
  case object Nov extends Month
  case object Dec extends Month
  
  sealed abstract class DayOfWeek(val numberRepresentation:Int)
  case object Sun extends DayOfWeek(1) // 1
  case object Mon extends DayOfWeek(2) // 2
  case object Tues extends DayOfWeek(3) // 3
  case object Wed extends DayOfWeek(4) // 4
  case object Thur extends DayOfWeek(5) // 5
  case object Fri extends DayOfWeek(6) // 6
  case object Sat extends DayOfWeek(7) // 7
  

}