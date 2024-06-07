package net.kotlinx.domain.job.trigger

import mu.KotlinLogging
import net.kotlinx.domain.job.Job

/**
 * 간단 홀더
 */
object JobHolder {

    private val log = KotlinLogging.logger {}

    /** 기본 스래드로컬 */
    val JOB = ThreadLocal<Job>()


}