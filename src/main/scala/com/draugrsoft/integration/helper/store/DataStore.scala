package com.draugrsoft.integration.helper.store

import scala.concurrent.Future
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import com.typesafe.config.Config

/**
 * Stores everything in a hashmap
 */

private [integration] object DefaultJobInstanceDataStore extends DataStore {

  override implicit val configOpt = None

  val mutableMap: scala.collection.mutable.Map[Int, JobInstanceData] = scala.collection.mutable.Map()

  def create(data: JobInstanceData): Future[Boolean] = {
    mutableMap += (data.id -> data)
    Future.successful(true)
  }
  def read: Future[HistoricalData] = {
    val data = mutableMap.values.toList
    Future.successful(HistoricalData(data))
  }
  
  def delete(data:JobInstanceData): Future[Boolean] = {
    mutableMap.remove(data.id).fold[Future[Boolean]](Future.successful(false))(_ => Future.successful(true))
  }

  def clear = {
    mutableMap.clear()
  }
}

 
private [integration] trait DataStore {

  implicit val configOpt: Option[Config]

  def create(data: JobInstanceData): Future[Boolean]
  def read: Future[HistoricalData]
  def delete(data:JobInstanceData): Future[Boolean]
}