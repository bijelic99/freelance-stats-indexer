package com.freelanceStats.models

import com.freelanceStats.commons.models.RawJob

case class IndexingError(
    job: RawJob,
    cause: Throwable
) extends Exception(cause)
