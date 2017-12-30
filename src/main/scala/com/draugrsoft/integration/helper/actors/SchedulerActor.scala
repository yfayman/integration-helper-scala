package com.draugrsoft.integration.helper.actors

import akka.actor._
import akka.event.Logging

object SchedulerActor {
  
  def props(dataStoreActor:ActorRef):Props = Props(classOf[SchedulerActor],dataStoreActor)
  
}

class SchedulerActor(dataStoreActor: ActorRef) extends Actor{
  
  val log = Logging(context.system, this)
  
  def receive = {
    case a => log.info(s"Received unknown msg type $a")
  }
  
}