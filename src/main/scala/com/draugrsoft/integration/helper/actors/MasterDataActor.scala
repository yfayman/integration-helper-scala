package com.draugrsoft.integration.helper.actors

import akka.actor.Actor
import akka.actor.Props
import com.draugrsoft.integration.helper.store.DataStore
import com.draugrsoft.integration.helper.messages.CommonActorMessages._

object MasterDataActor {
  def props(dataStore:DataStore):Props = Props(classOf[MasterDataActor],dataStore)
}

class MasterDataActor(dataStore:DataStore) extends Actor{
  
  implicit val ec = context.dispatcher
  
  def receive = {
    case SaveDataRequest(data) => {
      val sendTo = sender
      dataStore.create(data).onComplete { tr => sendTo ! SaveDataResponse(tr.getOrElse(false)) }
    }
    case GetHistoricalInfoRequest => {
      val sendTo = sender
      dataStore.read.onSuccess({case HistoricalData(data) => sendTo ! GetHistoricalInfoResponse(data) })
    }
    case _ =>
  }
}