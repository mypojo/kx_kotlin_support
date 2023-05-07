package net.kotlinx.core2.calculator

import net.kotlinx.core1.time.TimeUtil
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.milliseconds

/**
 * 배치 작업시 얼마나 걸릴지 대략 측정
 * */
class BatchEstimate(
    /** 전체 수 */
    val totalCount: Long,
    /** 완료된 수 */
    val completedCount: Long,
    /** 이미 완료되서 스킵된 카운트 */
    val skipCount: Long,
) {

    val progressRate: BigDecimal = run {
        if (completedCount == 0L) return@run BigDecimal.ZERO
        completedCount.toBigDecimal().setScale(2) * 100.toBigDecimal() / totalCount.toBigDecimal()
    }

    /** 예상 종료 시간 */
    fun estimateEndTime(startTime: LocalDateTime): LocalDateTime? {

        if (completedCount == 0L) return null

        val now = LocalDateTime.now()
        val interval = TimeUtil.interval(startTime, now)

        val remain = interval * totalCount / completedCount
        return now.plusSeconds(remain.milliseconds.inWholeSeconds)
    }


}