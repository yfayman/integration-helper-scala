package com.draugrsoft.integration.helper.actors.test

import akka.actor.Props
import akka.actor.Actor

/**
 * This actor takes the place of the dispatcher that sits under the JobMasterActor
 * so that the JobMaster Actor can be tested
 */
object DummyActor {
  def props: Props = Props(classOf[DummyActor])
}

class DummyActor extends Actor {
  def receive = {
    case _ => ()
  }
}