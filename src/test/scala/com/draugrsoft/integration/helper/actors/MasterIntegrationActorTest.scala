package com.draugrsoft.integration.helper.actors

import org.scalatest.{ WordSpecLike, MustMatchers }
import akka.testkit.TestKit
import akka.actor._
import akka.testkit.TestActorRef
import scala.concurrent.duration.FiniteDuration

import java.util.concurrent.TimeUnit

import akka.testkit.ImplicitSender
import akka.testkit.DefaultTimeout

import com.draugrsoft.integration.helper.actors.MasterJobActor._

import akka.pattern.ask
import scala.concurrent.Await

import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages._
import com.draugrsoft.integration.helper.messages.CommonActorMessages._

import scala.concurrent.duration.Duration

import com.draugrsoft.integration.helper.constants.JobStatus._

import akka.testkit.CallingThreadDispatcher

import com.draugrsoft.integration.helper.constants.JobAction.StartAction
import com.draugrsoft.integration.helper.routes.DummyJobActor
import com.draugrsoft.integration.helper.constants.JobAction.StopAction

class MasterIntegrationActorTest extends TestKit(ActorSystem("testsystem"))
    with WordSpecLike
    with MustMatchers
    with ImplicitSender
    with DefaultTimeout
    with StopSystemAfterAll {

  import MasterIntegrationActor._

  "A MasterIntegrationActor" must {

    "create and be able to read status " in {
      val testIntegration = Integration("integration1", JobWithProps("test jerb", DummyJobActor.props) :: Nil)
      val mainAct = system.actorOf(MasterIntegrationActor.props(testIntegration))

      mainAct ! IntegrationStatusRequest
      expectMsg(IntegrationRecentStatus("integration1", List(JobRecentStatus("test jerb", INITIALIZING))))

      val statusOneFuture = mainAct.ask(IntegrationStatusRequest).mapTo[IntegrationRecentStatus]
      val integrationStatusOne = Await.result(statusOneFuture, Duration.Inf)
      assertResult(1)(integrationStatusOne.jobs.size)
      assertResult("test jerb")(integrationStatusOne.jobs(0).name)

      Await.result(mainAct.ask(UpdateJobRequest("test jerb", JobAction(StartAction,Nil))), Duration.Inf) match {
        case UpdateJobResponse(jid) if jid.start.isDefined => assert(true)
        case _ => assert(false)
      }
      mainAct ! UpdateJobRequest("made up jerb", JobAction(StartAction,Nil))
      expectMsg(JobNotFound)

      // Make sure that the started job is running
      val statusTwoFuture = mainAct.ask(IntegrationStatusRequest).mapTo[IntegrationRecentStatus]
      val integrationStatusTwo = Await.result(statusTwoFuture, Duration.Inf)
      assertResult(1)(integrationStatusTwo.jobs.size)
      assertResult("test jerb")(integrationStatusTwo.jobs(0).name)


    }

    "be able to create 2 jobs with unique names" in {
      val testIntegration = Integration("integration1", JobWithProps("test jerb", DummyJobActor.props) :: JobWithProps("test jerb 2", DummyActor.props) :: Nil)
      val mainAct = system.actorOf(MasterIntegrationActor.props(testIntegration))

      val statusOneFuture = mainAct.ask(IntegrationStatusRequest).mapTo[IntegrationRecentStatus]
      val integrationStatusOne = Await.result(statusOneFuture, Duration.Inf)

      assertResult(2)(integrationStatusOne.jobs.size )

    }

    "be able to get detailed status information" in {
      val testIntegration = Integration("integration1", JobWithProps("test jerb", DummyJobActor.props) :: JobWithProps("test jerb 2", DummyActor.props) :: Nil)
      val mainAct = system.actorOf(MasterIntegrationActor.props(testIntegration))

      mainAct ! JobStatusRequest("test jerb")

      // Job that exists
      Await.result(mainAct.ask(JobStatusRequest("test jerb")), Duration.Inf) match {
        case JobStatusResponse(jidOpt) if jidOpt.isDefined => assert(true)
        case _ => assert(false)
      }
      //Job that is made up
      Await.result(mainAct.ask(JobStatusRequest("dey took ewr jerbs")), Duration.Inf) match {
        case JobStatusResponse(jidOpt) if jidOpt.isEmpty => assert(true)
        case _ => assert(false)
      }

    }

    "be able to execute an action on all jobs" in {
      val testIntegration = Integration("integration1", JobWithProps("test jerb", DummyJobActor.props) :: JobWithProps("test jerb 2", DummyActor.props) :: Nil)
      val mainAct = system.actorOf(MasterIntegrationActor.props(testIntegration))

      val updateJobResponseFuture = mainAct.ask(UpdateJobsRequest(StartAction)).mapTo[List[UpdateJobResponse]]
      val updateJobResponse = Await.result(updateJobResponseFuture, Duration.Inf)
      assert(updateJobResponse.nonEmpty)
      updateJobResponse.foreach { ujr =>
        {
          assertResult(RUNNING)(ujr.data.status)
          assert(ujr.data.start.isDefined)
          assert(ujr.data.end.isEmpty)
        }
      }

      val updateJobResponseFuture2 = mainAct.ask(UpdateJobsRequest(StopAction)).mapTo[List[UpdateJobResponse]]

      val updateJobResponse2 = Await.result(updateJobResponseFuture2, Duration.Inf)
      assert(updateJobResponse2.nonEmpty)
      updateJobResponse2.foreach { ujr =>
        {
          assertResult(STOPPED)(ujr.data.status)
          assert(ujr.data.start.isDefined)
          assert(ujr.data.end.isDefined)
        }
      }
    }

    "be able to check the status on individual and all jobs" in {
      val testIntegration = Integration("integration1", JobWithProps("test jerb", DummyJobActor.props) :: JobWithProps("test jerb 2", DummyActor.props) :: Nil)
      val mainAct = system.actorOf(MasterIntegrationActor.props(testIntegration))

      val jobStatiResponseFutureOne = mainAct.ask(JobStatiRequest("test jerb")).mapTo[JobStatiResponse]

      val jobStatiResponseOne = Await.result(jobStatiResponseFutureOne, Duration.Inf)
      assert(jobStatiResponseOne.data.nonEmpty)
      jobStatiResponseOne.data.foreach(jid => {
        assertResult(INITIALIZING)(jid.status)
        assert(jid.start.isEmpty)
        assert(jid.messages.isEmpty)
        assert(jid.attributes.isEmpty)
      })

      val jobStatiResponseFutureTwo = mainAct.ask(JobStatiRequest("job that doesn't exist")).mapTo[JobStatiResponse]
      val jobStatiResponseTwo = Await.result(jobStatiResponseFutureTwo, Duration.Inf)
      assert(jobStatiResponseTwo.data.isEmpty)
      
      val jobStatusResponseFuture = mainAct.ask(JobStatusRequest("test jerb")).mapTo[JobStatusResponse]
      val jobStatusResponse = Await.result(jobStatusResponseFuture, Duration.Inf)
      assert(jobStatusResponse.data.isDefined)
      assertResult(INITIALIZING)(jobStatusResponse.data.get.status)
    }

  }

}