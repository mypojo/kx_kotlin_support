package net.kotlinx.logback

import ch.qos.logback.classic.Level
import kotlin.reflect.KClass

/** 트레이스로 변경 */
fun KClass<*>.logTrace(depth: Int = 0): String = logLevelTo(depth, Level.TRACE)

/** 디버그로 변경 */
fun KClass<*>.logDebug(depth: Int = 0): String = logLevelTo(depth, Level.DEBUG)

/**
 * 해당 패키지의 로그 레벨을 변경해준다. 간단한 테스트 할때 하드코딩 방지해줌
 * @param depth 이 숫자만큼 패키지 경로를 생략함 -> 더 넓은 범위 변경
 * @return 변경된 로그패스 리턴
 * */
fun KClass<*>.logLevelTo(depth: Int = 0, level: Level): String {
    val packageName = this.qualifiedName!!
    val logPath = packageName.split(".").let { it.take(it.size - depth).joinToString(",") }
    LogBackUtil.logLevelTo(logPath, level)
    return logPath
}