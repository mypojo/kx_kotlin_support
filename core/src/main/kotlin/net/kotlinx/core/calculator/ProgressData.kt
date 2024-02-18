package net.kotlinx.core.calculator

import net.kotlinx.core.time.toTimeString
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

/**
 * Number 로 계산 금지!
 * */
class ProgressData(
    /** 전체 수  */
    totalInput: Number,
    /** 완료된 수 */
    completedInput: Number = 0L,
    /** 작업 시작시간 */
    val startTime: LocalDateTime? = null,
    /** 이미 완료되서 스킵된 카운트 */
    skipedInput: Number = 0L,
    /** 비율 스케일 */
    private val rateScale: Int = 1,
) {

    /** 전체 수  */
    val total: Long = totalInput.toLong()

    /** 완료된 수 */
    val completed: Long = completedInput.toLong()

    /** 이미 완료되서 스킵된 카운트 */
    val skiped: Long = skipedInput.toLong()

    /** 지금시간 */
    val now: LocalDateTime by lazy { LocalDateTime.now() }

    //==================================================== 계산값 ======================================================

    /** 진행율% */
    val progressRate: BigDecimal by lazy {
        if (completed == 0L) BigDecimal.ZERO else completed.toBigDecimal().setScale(rateScale) * 100.toBigDecimal() / total.toBigDecimal()
    }

    /** 진행 시간 */
    val progressTime: Long = ChronoUnit.MILLIS.between(startTime!!, now)

    /** 전체 예상 시간 */
    val totalTime: Long = if (completed == 0L) 0L else progressTime * total / completed

    /** 남은 시간 */
    val remainTime: Long = abs(totalTime - progressTime)

    override fun toString(): String {
        val remainTimeStr = if (progressRate <= 0.1.toBigDecimal()) "unknown" else remainTime.toTimeString().toString()
        return " => ${completed.toString().padStart(total.toString().length)}/${total} (${progressRate.toString().padStart(3)}%)" +
                " [진행시간 ${progressTime.toTimeString()}] [남은예상시간 ${remainTimeStr}]"
    }

    /** 예상 종료 시간 (잘 안씀) */
    val estimateEndTime: LocalDateTime? by lazy {
        if (completed == 0L) null else now.plusSeconds(remainTime.milliseconds.inWholeSeconds)
    }
}
