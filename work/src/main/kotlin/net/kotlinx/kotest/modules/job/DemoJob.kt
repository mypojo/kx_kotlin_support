package net.kotlinx.kotest.modules.job

import mu.KotlinLogging
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobTasklet
import java.util.concurrent.atomic.AtomicLong

class DemoJob : JobTasklet {

    private val log = KotlinLogging.logger {}

    override suspend fun doRun(job: Job) {
        log.debug { "데모 잡이 실행됩니다.. " }
        cnt.incrementAndGet()
    }

    companion object {
        val cnt = AtomicLong(0)
    }

}