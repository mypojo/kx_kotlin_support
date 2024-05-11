package net.kotlinx.domain.job

import net.kotlinx.aws.fargate.FargateUtil
import net.kotlinx.number.halfUp
import java.math.BigDecimal
import java.time.LocalDateTime


data class JobSta(
    val job: Job,
    val cnt: Int,
    val successCnt: Int,
    val failCnt: Int,
    val startMax: LocalDateTime,
    val startMin: LocalDateTime,
    val lastExecute: LocalDateTime,
    val sumOfDuration: Long,
    val avgOfDuration: Long,
    val maxOfDuration: Long,
    /** 최근 일주일 비용 기준으로 계산.. 환율 1400원 */
    val sumOfCost: BigDecimal,
)

fun Collection<Job>.toJobSta(): JobSta {
    val jobs = this
    val sumOfDuration = jobs.sumOf { it.toIntervalMills() ?: 0 }
    val monthTime = (sumOfDuration / 7 * 30.5).toLong()
    return JobSta(
        jobs.first(),
        jobs.size,
        jobs.count { it.jobStatus == JobStatus.SUCCEEDED },
        jobs.count { it.jobStatus != JobStatus.SUCCEEDED },
        jobs.mapNotNull { it.startTime }.maxOf { it },
        jobs.mapNotNull { it.startTime }.minOf { it },
        jobs.mapNotNull { it.startTime }.maxOf { it },
        sumOfDuration,
        sumOfDuration / jobs.size,
        jobs.mapNotNull { it.toIntervalMills() }.maxOf { it },
        (FargateUtil.cost(1.0, 2.0, monthTime) * 1400 / 10000).halfUp(1),
    )
}

