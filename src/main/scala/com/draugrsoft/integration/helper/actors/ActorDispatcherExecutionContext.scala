package com.draugrsoft.integration.helper.actors

import akka.actor.Actor

trait ActorDispatcherExecutionContext {
  
  this: Actor =>
  
  implicit val ec = context.dispatcher
  
}