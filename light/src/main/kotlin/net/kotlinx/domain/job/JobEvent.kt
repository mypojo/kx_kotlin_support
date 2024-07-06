package net.kotlinx.domain.job

/** 잡 성공 */
data class JobSuccessEvent(val job: Job)

/** 잡 실패 */
data class JobFailEvent(val job: Job, val err: Throwable)