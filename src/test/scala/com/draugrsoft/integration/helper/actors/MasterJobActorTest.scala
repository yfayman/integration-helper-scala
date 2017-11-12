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
import com.draugrsoft.integration.helper.constants.MessageLevel._

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
      expectMsg(JobStatusResponse(Some(JobInstanceData(0, "test", None, None, Nil, Nil, Map(), INITIALIZING))))

      val startReq = JobAction(StartAction, Nil)

      Await.result(masterJob.ask(startReq), Duration.Inf) match {
        case UpdateJobResponse(jid) if jid.start.isDefined => assert(true)
        case _ => assert(false)
      }

      Await.result(masterJob.ask(JobStatusRequest("test")), Duration.Inf) match {
        case JobStatusResponse(opt) if opt.isDefined && opt.get.status == RUNNING && opt.get.start.isDefined => assert(true)
        case _ => assert(false)
      }

      val stopReq = JobAction(StopAction, Nil)

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

      masterJob ! HistoricalData(JobInstanceData(55, "test", None, None, Nil, Nil, Map.empty, COMPLETED)
        :: JobInstanceData(43, "test", None, None, Nil, Nil, Map.empty, COMPLETED) :: Nil)

      val statusResponseAfterFuture = masterJob.ask(JobStatiRequest("test")).mapTo[JobStatiResponse]
      val statusResponseAfter = Await.result(statusResponseAfterFuture, Duration.Inf)
      assert(statusResponseAfter.data.tail.nonEmpty)
    }

    "successfully receive and process requests from dispatcher " in {
      val masterJob = system.actorOf(MasterJobActor.props(AddActor.props, "add test"))
      masterJob ! JobAction(StartAction, JobParam("1", "4") :: JobParam("2","75") :: Nil)
      
      Thread.sleep(200)
      
      Await.result(masterJob.ask(JobStatusRequest("add test")), Duration.Inf) match { 
        case JobStatusResponse(opt) => {
          assert(opt.isDefined)
          val resultJobInstanceData = opt.get
          assertResult(COMPLETED)(resultJobInstanceData.status)
          assertResult(2)(resultJobInstanceData.messages.size) // 2 for params
          assertResult(1)(resultJobInstanceData.attributes.size) // 1 for result
          assert(!resultJobInstanceData.attributes.isEmpty)
          val answerOpt = resultJobInstanceData.attributes.get("answer") 
          assert(answerOpt.isDefined)
          assertResult("79")(answerOpt.get)
        }
        case _ => assert(false)
      }
      
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

object AddActor {
  def props: Props = Props(classOf[AddActor])
}

class AddActor extends Actor {
  def receive = {
    case JobAction(action, params) => {     
        params.foreach { param => context.parent ! JobMessage(s"received $param.name | $param.value", INFO) }
        val sum = params.map { _.value.toInt }.sum
        sender() ! SendResult(Map("answer" -> sum.toString), Nil)
    }
  }
}