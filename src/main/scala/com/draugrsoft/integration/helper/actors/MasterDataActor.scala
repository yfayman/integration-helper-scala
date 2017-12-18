package com.draugrsoft.integration.helper.actors

import akka.actor.Actor
import akka.actor.Props
import com.draugrsoft.integration.helper.store.DataStore
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import scala.util.{ Success, Failure }
import akka.event.Logging
import akka.actor.Status

private [integration] object MasterDataActor {
  def props(dataStore: DataStore): Props = Props(classOf[MasterDataActor], dataStore)
}

class MasterDataActor(dataStore: DataStore) extends Actor
  with ActorDispatcherExecutionContext {

  val log = Logging(context.system, this)

  def receive = {
    case SaveDataRequest(data) => {
      val sendTo = sender
      dataStore.create(data).onComplete{
        case Success(s) => sendTo ! SaveDataResponse(s)
        case Failure(e) =>Status.Failure(e)
      }
    }
    case GetHistoricalInfoRequest => {
      val sendTo = sender
      dataStore.read.onComplete({
        case Success(hd) => sendTo ! GetHistoricalInfoResponse(hd.data)
        case Failure(e)  => sendTo ! Status.Failure(e)
      })
    }
    case _ =>
  }
}