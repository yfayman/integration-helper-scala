package com.draugrsoft.integration.helper.actors

import akka.util.Timeout
import java.util.concurrent.TimeUnit

trait DebugTimeout {
  
    implicit val timeout = Timeout(50, TimeUnit.SECONDS)
}