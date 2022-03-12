package com.freelanceStats.components.indexedJobCreator

import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import com.freelanceStats.commons.models.RawJob
import com.freelanceStats.commons.models.indexedJob.IndexedJob
import com.freelanceStats.models.{IndexingError, IndexingSuccess}
import com.freelanceStats.s3Client.models.FileReference

trait IndexedJobCreator {
  def apply: Flow[
    (RawJob, Option[IndexedJob], FileReference, Source[ByteString, _]),
    Either[IndexingError, IndexingSuccess],
    _
  ]
}
