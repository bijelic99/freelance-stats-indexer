package com.freelanceStats.modules

import com.freelanceStats.components.indexedJobCreator.{
  FreelancerIndexedJobCreator,
  IndexedJobCreator
}
import com.google.inject.AbstractModule

class FreelancerModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[IndexedJobCreator]).to(classOf[FreelancerIndexedJobCreator])
  }
}
