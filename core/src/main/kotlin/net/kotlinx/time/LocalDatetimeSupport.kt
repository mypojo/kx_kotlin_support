package net.kotlinx.time

import net.kotlinx.core.PackageNameSupport
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.time.toKotlinDuration

object LocalDatetimeSupport : PackageNameSupport

/** 한국 시간으로 포매팅 (로그 확인용) - 기본 시분초까지 */
fun LocalDateTime.toKr01(): String = TimeFormat.YMDHMS_K01[this]

/** 한국 시간으로 포매팅 (로그 확인용) - 시분까지 */
fun LocalDateTime.toYmdhmKr01(): String = TimeFormat.YMDHM_K01[this]

/** 한국 시간으로 포매팅 (로그 확인용) - 기본 시분초까지 */
fun LocalDateTime.toIso(): String = TimeFormat.ISO[this]

/** YMD  */
fun LocalDateTime.toYmd(): String = TimeFormat.YMD[this]

/** YMDHM_F01  */
fun LocalDateTime.toF01(): String = TimeFormat.YMDHM_F01[this]

/**
 * this 값을 시작시간으로 보고 둘 사이의 간격을 구함.
 * 자꾸 헷갈려서 그냥 함수로 등록했다.
 * @param endTime 종료시간.
 *
 * ex) 시작부터 지금까지 걸린시간 startTime.between()
 * ex) 지금부터 종료까지 남은시간 LocalDateTime.now().between(endTime)
 * */
fun LocalDateTime.between(endTime: LocalDateTime = LocalDateTime.now()): kotlin.time.Duration = Duration.between(this, endTime).toKotlinDuration()

/** 해당 시간과 현재(옵션)와의 시간차이를 간단하게 구한다. */
fun LocalDateTime.toTimeString(endTime: LocalDateTime = LocalDateTime.now()): TimeString = (endTime.toLong() - this.toLong()).toTimeString()


/**
 * 노션 등에서 날짜/시간 이렇게 두가지 타입을 같이 줄때 사용
 * */
fun LocalDateTime.isDate(): Boolean = this.hour == 0 && this.minute == 0 && this.second == 0 && this.nano == 0

//==================================================== 존 관련 (테스트 필요) ======================================================

/** Instant 변환 */
fun LocalDateTime.toInstant(zoneId: ZoneId = TimeUtil.SEOUL): Instant = this.atZone(zoneId).toInstant()!!

/** 밀리초 변환 */
fun LocalDateTime.toLong(zoneId: ZoneId = TimeUtil.SEOUL): Long = this.atZone(zoneId).toInstant().toEpochMilli()

/** Date 로 컴버팅*/
fun LocalDateTime.toDate(zoneId: ZoneId = TimeUtil.SEOUL): Date = Date.from(this.atZone(zoneId).toInstant())

//==================================================== 절삭 ======================================================

/**
 * 밀리초 단위를 제거하고 리턴하는 참고 샘플 코드
 * SECONDS 로 해야 밀리초가 제거되고 SECONDS만 남는다
 *  */
fun LocalDateTime.truncatedMills(): LocalDateTime = this.truncatedTo(ChronoUnit.SECONDS)

