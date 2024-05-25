package net.kotlinx.domain.eventLog.publisher

import mu.KotlinLogging
import net.kotlinx.domain.eventLog.EventPublishClient
import net.kotlinx.domain.job.Job
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.time.toLong

class EventJobPublisherSample {

    private val log = KotlinLogging.logger {}

    private val client by koinLazy<EventPublishClient>()

    fun pubEventJob(job: Job) {

        client.pub { event ->
            event.logLink = job.toLogLink()
            event.eventStatus = job.jobStatus.toString()
            event.errMsg = job.jobErrMsg
            event.eventDiv = job.toKeyString()
            job.endTime?.let {
                event.eventMills = job.endTime!!.toLong() - job.startTime!!.toLong()
            }
            true
        }
    }
}