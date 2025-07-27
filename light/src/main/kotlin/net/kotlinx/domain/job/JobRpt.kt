package net.kotlinx.domain.job

import java.math.BigDecimal
import java.time.LocalDateTime


data class JobRpt(
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

