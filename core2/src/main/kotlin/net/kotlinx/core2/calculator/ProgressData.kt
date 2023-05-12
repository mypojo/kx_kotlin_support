package net.kotlinx.core2.calculator

import net.kotlinx.core1.time.TimeUtil
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.milliseconds

data class ProgressData(
    /** 전체 수  */
    val total: Long,
    /** 완료된 수 */
    val completed: Long = 0,
    /** 작업 시작시간 */
    val startTime: LocalDateTime? = null,
    /** 이미 완료되서 스킵된 카운트 */
    val skiped: Long = 0,
) {

    /** 지금시간 */
    val now: LocalDateTime by lazy { LocalDateTime.now() }

    //==================================================== 계산값 ======================================================

    /** 진행율% */
    val progressRate: BigDecimal by lazy {
        if (completed == 0L) BigDecimal.ZERO else completed.toBigDecimal().setScale(2) * 100.toBigDecimal() / total.toBigDecimal()
    }

    /** 진행 시간 */
    val progressTime: Long = TimeUtil.interval(startTime!!, now)

    /** 전체 예상 시간 */
    val totalTime: Long = if (completed == 0L) 0L else progressTime * total / completed

    /** 남은 시간 */
    val remainTime: Long = totalTime - progressTime

    /** 예상 종료 시간 */
    val estimateEndTime: LocalDateTime? by lazy {
        if (completed == 0L) null else now.plusSeconds(remainTime.milliseconds.inWholeSeconds)
    }
}
