package com.draugrsoft.integration.helper.actors.test

import akka.util.Timeout
import java.util.concurrent.TimeUnit

trait DebugTimeout {
  
    implicit val timeout = Timeout(500, TimeUnit.SECONDS)
}