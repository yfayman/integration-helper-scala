package com.draugrsoft.integration.helper.actors

import akka.actor.Actor
import akka.actor.Props
import com.draugrsoft.integration.helper.store.DataStore
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import scala.util.{ Success, Failure }
import akka.event.Logging
import akka.actor.Status
import akka.pattern.ask
import com.draugrsoft.integration.helper.store.DefaultJobInstanceDataStore
import scala.util.Try
import scala.concurrent.Future

private[integration] object MasterDataActor {
  def props(dataStore: DataStore): Props = Props(classOf[MasterDataActor], dataStore)

  case object RevertToPrimaryDataStore

}

class MasterDataActor(dataStore: DataStore) extends Actor
  with ActorDispatcherExecutionContext
  with FiveSecondTimeout {

  import MasterDataActor._

  val primaryDataStore = dataStore
  val fallbackDataStore = DefaultJobInstanceDataStore

  var currentDataStore = primaryDataStore

  val log = Logging(context.system, this)

  def receive = {
    case SaveDataRequest(data) => {
      val sendTo = sender
      currentDataStore.create(data).onComplete {
        case Success(s) => sendTo ! SaveDataResponse(s)
        case Failure(e) => context.parent ! Status.Failure(e)
      }
    }
    case GetHistoricalInfoRequest => {
      val requestor = sender
      currentDataStore.read.onComplete({
        case Success(hd) => requestor ! GetHistoricalInfoResponse(hd.data)
        case Failure(e)  => context.parent ! Status.Failure(e)
      })
    }
    case RevertToPrimaryDataStore => {
      fallbackDataStore.read.onComplete({
        case Success(hd) => {
          Future.sequence(
            hd.data.map(jid => {
              self.ask(jid).mapTo[SaveDataResponse]
            })).onComplete({
              case Success(sdr) => {
                if (sdr.forall(_.success)) { currentDataStore = primaryDataStore }
              }
              case Failure(e) => context.parent ! Status.Failure(e)
            })
        }
        case Failure(e) => context.parent ! Status.Failure(e)
      })

    }
    case a => {
      log.warning(s"Got an unknown message $a")
    }
  }

  /**
   * If it's restarting, we will want to switch to fallbackDataStore
   */
  override def postRestart(e: Throwable) = {
    currentDataStore = fallbackDataStore
  }
}