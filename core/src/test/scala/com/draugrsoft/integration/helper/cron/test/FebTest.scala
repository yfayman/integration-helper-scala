package com.draugrsoft.integration.helper.cron.test

import org.scalatest.WordSpec
import org.scalatest.Matchers
import com.draugrsoft.integration.helper.constants.TimeConstant.Feb

class FebTest  extends WordSpec with Matchers{
  
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
  
}