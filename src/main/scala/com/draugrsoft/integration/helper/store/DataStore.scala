package com.draugrsoft.integration.helper.store

import scala.concurrent.Future
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import com.typesafe.config.Config

object DataStore {

  object DefaultJobInstanceDataStore extends DataStore {

    override implicit val configOpt = None

    override def create(hd: JobInstanceData): Future[Boolean] = Future.successful(true)
    override def read: Future[HistoricalData] = Future.successful(HistoricalData(Nil))
  }

}

trait DataStore {

  implicit val configOpt: Option[Config]

  def create(data: JobInstanceData): Future[Boolean]
  def read: Future[HistoricalData]
}