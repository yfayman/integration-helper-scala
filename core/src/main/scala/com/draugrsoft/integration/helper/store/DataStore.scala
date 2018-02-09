package com.draugrsoft.integration.helper.store

import scala.concurrent.Future
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import com.typesafe.config.Config
import java.util.concurrent.atomic.AtomicInteger

/**
 * Stores everything in a hashmap
 */

private [integration] object DefaultJobInstanceDataStore extends DataStore {

  override implicit val configOpt = None

  val mutableMap: scala.collection.mutable.Map[Int, JobInstanceData] = scala.collection.mutable.Map()
  
  val jobInstanceIdTracker:AtomicInteger = new AtomicInteger(1)
  val schedulerIdTracker:AtomicInteger = new AtomicInteger(1)

  def save(data: JobInstanceData): Future[Int] = {
    val jobInstanceId = data.id match{
      case Some(id) => id
      case None =>   jobInstanceIdTracker.get
    }    
    mutableMap += (jobInstanceId -> data)
    Future.successful(jobInstanceId)
  }
  def read: Future[HistoricalData] = {
    val data = mutableMap.values.toList
    Future.successful(HistoricalData(data))
  }
  
  def delete(data:JobInstanceData): Future[Boolean] = {
    data.id match{
      case Some(id) => {
         mutableMap.remove(id)
                   .fold[Future[Boolean]](Future.successful(false))(_ => Future.successful(true))
      }
      case None => Future.successful(false)
    }
   
  }

  def clear = {
    mutableMap.clear()
  }
}

 
private [integration] trait DataStore {

  implicit val configOpt: Option[Config]

  def save(data: JobInstanceData): Future[Int]
  def read: Future[HistoricalData]
  def delete(data:JobInstanceData): Future[Boolean]
}