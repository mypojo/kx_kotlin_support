package net.kotlinx.core.time

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.toKotlinDuration


/** 한국 시간으로 포매팅 (로그 확인용) - 기본 시분초까지 */
inline fun LocalDateTime.toKr01(): String = TimeFormat.YMDHMS_K01[this]

/** 한국 시간으로 포매팅 (로그 확인용) - 시분까지 */
inline fun LocalDateTime.toYmdhmKr01(): String = TimeFormat.YMDHM_K01[this]

/** 한국 시간으로 포매팅 (로그 확인용) - 기본 시분초까지 */
inline fun LocalDateTime.toIso(): String = TimeFormat.ISO[this]

/** YMD  */
inline fun LocalDateTime.toYmd(): String = TimeFormat.YMD[this]

/** 기본 존으로 변환 */
inline fun LocalDateTime.toZone(zoneId: ZoneId = TimeUtil.SEOUL): ZonedDateTime = this.atZone(zoneId)

/** 한국시간 기준 미리초 리턴 */
fun LocalDateTime.toLong(zoneId: ZoneId = TimeUtil.SEOUL): Long = this.atZone(zoneId).toInstant().toEpochMilli()

/** Date 로 컴버팅*/
fun LocalDateTime.toDate(zoneId: ZoneId = TimeUtil.SEOUL): Date = Date.from(this.atZone(zoneId).toInstant())

/**
 * this 값을 시작시간으로 보고 둘 사이의 간격을 구함.
 * 자꾸 헷갈려서 그냥 함수로 등록했다.
 * @param endTime 종료시간.
 *
 * ex) 시작부터 지금까지 걸린시간 startTime.between()
 * ex) 지금부터 종료까지 남은시간 LocalDateTime.now().between(endTime)
 * */
fun LocalDateTime.between(endTime: LocalDateTime = LocalDateTime.now()): kotlin.time.Duration = Duration.between(this, endTime).toKotlinDuration()

