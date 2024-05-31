package net.kotlinx.time

import java.time.LocalDate
import java.time.LocalDateTime

/** YMD  */
fun LocalDate.toYmd(): String = TimeFormat.YMD[this]

/** 루트 시간으로 변환 */
fun LocalDate.toLocalDateTime(): LocalDateTime = this.atTime(0, 0, 0, 0)

/**
 * YM
 * 월단위로 디렉토링할때 자주 사용됨
 *  */
fun LocalDate.toYm(): String = TimeFormat.YM[this]

/** Y-M-D (파라메터 입력용으로 많이 씀) */
fun LocalDate.toYmdF01(): String = TimeFormat.YMD_F01[this]

/** yyyy년MM월dd일(EEE) (간단 로깅용으로 많이씀) */
fun LocalDate.toYmdK01(): String = TimeFormat.YMD_K01[this]