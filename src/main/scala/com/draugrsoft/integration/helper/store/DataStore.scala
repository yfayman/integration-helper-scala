package com.draugrsoft.integration.helper.store

import scala.concurrent.Future
import com.draugrsoft.integration.helper.messages.CommonActorMessages._

object DataStore {
  
  object DefaultJobInstanceDataStore extends DataStore{
    def create(hd:HistoricalData):Future[Boolean] = ???
    def read : Future[HistoricalData] = ???
  }
  
}

trait DataStore{
  def create(data:HistoricalData):Future[Boolean]
  def read : Future[HistoricalData]
}