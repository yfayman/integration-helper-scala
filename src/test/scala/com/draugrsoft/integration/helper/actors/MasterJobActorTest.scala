package com.draugrsoft.integration.helper.actors

import org.scalatest.{ WordSpecLike, MustMatchers }
import akka.testkit.TestKit
import akka.actor._
import akka.testkit.TestActorRef
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import akka.testkit.ImplicitSender
import akka.testkit.DefaultTimeout
import com.draugrsoft.integration.helper.constants.JobStatus._
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import com.draugrsoft.integration.helper.constants.JobAction._
import scala.concurrent.duration.Duration
import akka.pattern.ask
import scala.concurrent.Await


class MasterJobActorTest extends TestKit(ActorSystem("testsystem"))
    with WordSpecLike
    with MustMatchers
    with ImplicitSender
    with DefaultTimeout
    with StopSystemAfterAll {

  import MasterJobActor._

  "A MasterJobActor" must {

    "respond to start/stop request" in {

      val masterJob = system.actorOf(MasterJobActor.props(DummyActor.props, "test"))

      masterJob ! JobStatusRequest("test")
      expectMsg(JobStatusResponse(Some(JobInstanceData(0, "test", None, None, Nil, Nil, INITIALIZING))))

      val startReq = JobAction(StartAction)

      Await.result(masterJob.ask(startReq), Duration.Inf) match {
        case UpdateJobResponse(jid) if jid.start.isDefined => assert(true)
        case _ => assert(false)
      }

      Await.result(masterJob.ask(JobStatusRequest("test")), Duration.Inf) match {
        case JobStatusResponse(opt) if opt.isDefined && opt.get.status == RUNNING && opt.get.start.isDefined => assert(true)
        case _ => assert(false)
      }

      val stopReq = JobAction(StopAction)

      Await.result(masterJob.ask(stopReq), Duration.Inf) match {
        case UpdateJobResponse(jid) if jid.end.isDefined => assert(true)
        case _ => assert(false)
      }

      Await.result(masterJob.ask(JobStatusRequest("test")), Duration.Inf) match {
        case JobStatusResponse(opt) if opt.isDefined && opt.get.status == STOPPED && opt.get.start.isDefined && opt.get.end.isDefined => assert(true)
        case _ => assert(false)
      }

    }

    "aggregate historical data " in {
      val masterJob = system.actorOf(MasterJobActor.props(DummyActor.props, "test"))
      val statusResponseFuture = masterJob.ask(JobStatiRequest("test")).mapTo[JobStatiResponse]
      val statusResponse = Await.result(statusResponseFuture, Duration.Inf)
      
      // just the current data
      assert(statusResponse.data.tail.isEmpty)

      masterJob ! HistoricalData(JobInstanceData(55, "test", None, None, Nil, Nil, COMPLETED)
        :: JobInstanceData(43, "test", None, None, Nil, Nil, COMPLETED) :: Nil)
           
      val statusResponseAfterFuture = masterJob.ask(JobStatiRequest("test")).mapTo[JobStatiResponse]
      val statusResponseAfter = Await.result(statusResponseAfterFuture, Duration.Inf)
      assert(statusResponseAfter.data.tail.nonEmpty)
    }

    "accept and respond to status requests" in {

    }
  }
}

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