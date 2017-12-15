package com.draugrsoft.integration.helper.actors

import akka.util.Timeout
import java.util.concurrent.TimeUnit

trait FiveSecondTimeout {

    implicit val timeout = Timeout(5, TimeUnit.SECONDS)
    
}