package net.kotlinx.core.time

import java.time.LocalDate

/** YMD  */
fun LocalDate.toYmd(): String = TimeFormat.YMD[this]

/** Y-M-D (파라메터 입력용으로 많이 씀) */
fun LocalDate.toYmdF01(): String = TimeFormat.YMD_F01[this]

/** yyyy년MM월dd일(EEE) (간단 로깅용으로 많이씀) */
fun LocalDate.toYmdK01(): String = TimeFormat.YMD_K01[this]