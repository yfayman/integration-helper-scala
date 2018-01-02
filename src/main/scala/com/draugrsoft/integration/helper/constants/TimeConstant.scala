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
  
  sealed trait DayOfWeek
  case object Mon extends DayOfWeek
  case object Tues extends DayOfWeek
  case object Wed extends DayOfWeek
  case object Thur extends DayOfWeek
  case object Fri extends DayOfWeek
  case object Sat extends DayOfWeek
  case object Sun extends DayOfWeek

}