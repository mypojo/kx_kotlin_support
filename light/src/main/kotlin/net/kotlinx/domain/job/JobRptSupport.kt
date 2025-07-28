package net.kotlinx.domain.job

import net.kotlinx.aws.fargate.FargateUtil
import net.kotlinx.number.halfUp

fun Collection<Job>.toJobSta(): JobRpt {
    val jobs = this
    val sumOfDuration = jobs.sumOf { it.intervalMills ?: 0 }
    val monthTime = (sumOfDuration / 7 * 30.5).toLong()
    return JobRpt(
        jobs.first(),
        jobs.size,
        jobs.count { it.jobStatus == JobStatus.SUCCEEDED },
        jobs.count { it.jobStatus != JobStatus.SUCCEEDED },
        jobs.mapNotNull { it.startTime }.maxOf { it },
        jobs.mapNotNull { it.startTime }.minOf { it },
        jobs.mapNotNull { it.startTime }.maxOf { it },
        sumOfDuration,
        sumOfDuration / jobs.size,
        jobs.mapNotNull { it.intervalMills }.maxOf { it },
        (FargateUtil.cost(1.0, 2.0, monthTime) * 1400 / 10000).halfUp(1),
    )
}