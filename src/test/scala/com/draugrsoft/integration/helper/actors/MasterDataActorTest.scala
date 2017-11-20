package com.draugrsoft.integration.helper.actors

import org.scalatest.{ WordSpecLike, MustMatchers }
import akka.testkit._
import akka.actor._
import com.draugrsoft.integration.helper.store.DataStore.DefaultJobInstanceDataStore
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

  val testDataStore = new DataStore {
    implicit val configOpt: Option[Config] = None
    
    val mutableMap:scala.collection.mutable.Map[Int,JobInstanceData] = scala.collection.mutable.Map()

    def create(data: JobInstanceData): Future[Boolean] = {
      mutableMap +=( data.id -> data)
      Future.successful(true)
    }
    def read: Future[HistoricalData] = Future.successful(HistoricalData(mutableMap.values.toList))
  }

  "A Master Data Actor" must {

    val dActor = system.actorOf(MasterDataActor.props(testDataStore))
    val sample1JobInstanceData = JobInstanceData(99, "abc", None, None, Nil, Nil, Map.empty, COMPLETED)
    val sample2JobInstanceData = JobInstanceData(100, "def", None, None, Nil, Nil, Map.empty, COMPLETED)
    
    
    "be able to successfully store new job instance data" in {
      dActor ! SaveDataRequest(sample1JobInstanceData)
      expectMsg(SaveDataResponse(true))
      
      dActor ! SaveDataRequest(sample2JobInstanceData)
      expectMsg(SaveDataResponse(true))
      
      dActor ! GetHistoricalInfoRequest
      expectMsg(GetHistoricalInfoResponse(sample2JobInstanceData :: sample1JobInstanceData :: Nil))
    }

  }

}
