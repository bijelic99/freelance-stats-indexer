package com.freelanceStats.components.indexerFlowFactory

import akka.stream.scaladsl.Flow
import com.freelanceStats.commons.models.RawJob
import com.freelanceStats.commons.models.indexedJob.IndexedJob

trait IndexerFlowFactory {
  def create: Flow[RawJob, IndexedJob, _]
}
