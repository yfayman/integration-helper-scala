package com.draugrsoft.integration.helper.actors

import org.scalatest.{ WordSpecLike, MustMatchers }
import akka.testkit._
import akka.actor._
import com.draugrsoft.integration.helper.store.DefaultJobInstanceDataStore
import com.draugrsoft.integration.helper.store.DataStore
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import com.typesafe.config.Config
import scala.concurrent.Future
import scala.collection.immutable.Map
import com.draugrsoft.integration.helper.constants.JobStatus.JobStatusEnum
import com.draugrsoft.integration.helper.constants.JobStatus.JobStatusEnum
import com.draugrsoft.integration.helper.constants.JobStatus.JobStatusEnum
import com.draugrsoft.integration.helper.constants.JobStatus.COMPLETED

class MasterDataActorTest extends TestKit(ActorSystem("testsystem"))
    with WordSpecLike
    with MustMatchers
    with ImplicitSender
    with DefaultTimeout
    with StopSystemAfterAll {


  "A Master Data Actor" must {

    val dActor = system.actorOf(MasterDataActor.props(DefaultJobInstanceDataStore))
    val sample1JobInstanceData = JobInstanceData(id = Some(99), 
                                                 name = "abc", 
                                                 start = None,
                                                 end = None,
                                                 params = Nil,
                                                 messages =  Nil,
                                                 attributes = Map.empty,
                                                 COMPLETED)
    val sample2JobInstanceData = JobInstanceData(id = Some(100),
                                                 name = "def",
                                                 start = None,
                                                 end = None,
                                                 params = Nil,
                                                 messages = Nil,
                                                 attributes = Map.empty,
                                                 COMPLETED)
    
    
    "be able to successfully store new job instance data" in {
      dActor ! SaveDataRequest(sample1JobInstanceData)
      expectMsg(SaveDataResponse(99, None))
      
      dActor ! SaveDataRequest(sample2JobInstanceData)
      expectMsg(SaveDataResponse(100, None))
      
      dActor ! GetHistoricalInfoRequest
      expectMsg(GetHistoricalInfoResponse(sample2JobInstanceData :: sample1JobInstanceData :: Nil))
    }

  }

}
