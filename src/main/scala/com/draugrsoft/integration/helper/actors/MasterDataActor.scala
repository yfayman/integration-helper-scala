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
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

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

  
/**
 * These messages will typically be sent from the MasterIntegrationActor
 */
  def receive = {
    case SaveDataRequest(data) => {
      val sendTo = sender
      currentDataStore.save(data).onComplete {
        case Success(id) => sendTo ! SaveDataResponse(Left(id))
        case Failure(throwable) => throwable match {
          case ex:Exception => sendTo ! SaveDataResponse(Right(ex))
          case t:Throwable => context.parent ! Status.Failure(t)
        }
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
          Future.sequence{
            hd.data.map(
              jid => self.ask(jid).mapTo[SaveDataResponse])
          }.onComplete({
            case Success(sdr) => {
              if (sdr.forall(_.idOrError.isLeft)) {
                currentDataStore = primaryDataStore
                fallbackDataStore.clear
              }else{
                //TODO
              }
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
   * and add a revert message in 30 seconds
   */
  override def postRestart(e: Throwable) = {
    currentDataStore = fallbackDataStore
    context.system.scheduler.scheduleOnce(
      Duration.create(30, TimeUnit.SECONDS),
      () => { self ! RevertToPrimaryDataStore })
    super.postRestart(e)
  }
}