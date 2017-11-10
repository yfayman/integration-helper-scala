package com.draugrsoft.integration.helper.actors

import akka.actor.Actor
import akka.actor.Props
import com.draugsoft.integration.helper.persist.Persist
import com.draugsoft.integration.helper.persist.Dummy

object PersistActor{
  
   def props(persist:Persist = new Dummy) : Props = Props(classOf[PersistActor],persist)
   
}

class PersistActor(persist:Persist) extends Actor{
  
  
  def receive = ???
  
  
 
  
}