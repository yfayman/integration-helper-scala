package com.draugrsoft.integration.helper.actors

import org.scalatest.{ WordSpecLike, MustMatchers }
import akka.testkit._
import akka.actor._
import com.draugrsoft.integration.helper.store.DefaultJobInstanceDataStore
import akka.pattern.ask
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ScheduleActorTest extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with DefaultTimeout
  with StopSystemAfterAll {

  import SchedulerActor._

  "A schedule actor" must {
    val dataStoreActor = system.actorOf(MasterDataActor.props(DefaultJobInstanceDataStore))
    val scheduleActor = system.actorOf(SchedulerActor.props(dataStoreActor))

    "respond to get request" in {

      //There should be no schedules at this point. Just want to ensure a response
      assert {
        Await.result(scheduleActor.ask(GetSchedule), Duration.Inf) match {
          case GetScheduleResponse(schedules) => schedules == Nil
          case _                              => false
        }
      }
    }

    "add new schedules" in {
      val everyFiftyFiveTrig = ScheduleSecondTrigger(55)
      val everySixtyFiveTrig = ScheduleSecondTrigger(65)

      //First let's add and make sure we get a response
      assert {
        Await.result(scheduleActor.ask(AddSchedule(everyFiftyFiveTrig)), Duration.Inf) match {
          case AddScheduleResponse(e) => e.isEmpty
          case _                      => false
        }
      }

      // Now let's make a get request to confirm it's been added
      assert {
        Await.result(scheduleActor.ask(GetSchedule), Duration.Inf) match {
          case GetScheduleResponse(schedules) => schedules == (everyFiftyFiveTrig :: Nil)
          case _                              => false
        }
      }

      //Add the other schedule
      assert {
        Await.result(scheduleActor.ask(AddSchedule(everySixtyFiveTrig)), Duration.Inf) match {
          case AddScheduleResponse(e) => e.isEmpty
          case _                      => false
        }
      }

      // Now let's make a get request to confirm it's been added
      assert {
        Await.result(scheduleActor.ask(GetSchedule), Duration.Inf) match {
          case GetScheduleResponse(schedules) => schedules.size == 2
          case _                              => false
        }
      }
    }

    "respond with not found when removing non-existing schedule" in {

      val everyNintyTrig = ScheduleSecondTrigger(90)

      assert {
        Await.result(scheduleActor.ask(RemoveSchedule(everyNintyTrig)), Duration.Inf) match {
          case ScheduleNotFound => true
          case _                => false
        }
      }
    }

    "successfully remove a schedule after adding" in {
      val everyEightyTrig = ScheduleSecondTrigger(80)

      // Add the schedule
      assert {
        Await.result(scheduleActor.ask(AddSchedule(everyEightyTrig)), Duration.Inf) match {
          case AddScheduleResponse(e) => e.isEmpty
          case _                      => false
        }
      }
      
      //now remove
      assert {
        Await.result(scheduleActor.ask(RemoveSchedule(everyEightyTrig)), Duration.Inf) match {
          case ScheduleNotFound => false
          case RemoveScheduleResponse(e) => e.isEmpty
          case _                => false
        }
      }
      
      //Do get request and confirm that the trigger is there
      assert {
        Await.result(scheduleActor.ask(GetSchedule), Duration.Inf) match {
          case GetScheduleResponse(schedules) => !schedules.filter(_ == everyEightyTrig).isEmpty
          case _                              => false
        }
      }
      
    }

  }

}