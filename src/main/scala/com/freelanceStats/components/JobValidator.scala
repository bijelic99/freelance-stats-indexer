package com.freelanceStats.components

import akka.stream.scaladsl.Flow
import com.freelanceStats.commons.models.indexedJob.{Hourly, IndexedJob}
import com.freelanceStats.components.JobValidator.{
  ErrorMessage,
  validityCriteria
}
import org.joda.time.DateTime

class JobValidator {
  def apply(): Flow[
    IndexedJob,
    Either[ErrorMessage, IndexedJob],
    _
  ] =
    Flow[IndexedJob]
      .map { job =>
        validityCriteria.lift(job).toLeft(job.copy(valid = true))
      }

}

object JobValidator {
  type ErrorMessage = String
  val validityCriteria: PartialFunction[IndexedJob, ErrorMessage] = {
    case job if job.id.trim.equals("")       => "Id can't be empty"
    case job if job.sourceId.trim.equals("") => "SourceId can't be empty"
    case job if job.source.trim.equals("")   => "Source can't be empty"
    case job if job.created.isAfter(DateTime.now()) =>
      "Creation date can't be in the future"
    case job if job.modified.isAfter(DateTime.now()) =>
      "Modification date can't be in the future"
    case job if job.title.trim.equals("")       => "Title can't be empty"
    case job if job.description.trim.equals("") => "Description can't be empty"
    case job if job.categories.isEmpty          => "Categories can't be empty"
    case IndexedJob(
          _,
          _,
          _,
          _,
          _,
          _,
          _,
          _,
          _,
          Hourly(_, true, None, _),
          _,
          _,
          _,
          _,
          _,
          _
        ) =>
      "If job is hourly and repeating repeat interval must be set"
    case job if job.positionType.isEmpty => "Position type needs to be set"
  }
}
