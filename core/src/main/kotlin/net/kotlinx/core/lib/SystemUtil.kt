package net.kotlinx.core.lib

import net.kotlinx.core.string.TextGrid
import net.kotlinx.core.string.toTextGrid
import java.lang.management.ManagementFactory
import java.net.Inet4Address


object SystemUtil {

    val IP: String by lazy { Inet4Address.getLocalHost().hostAddress }

    fun nowUsedMemory(): Long {
        Runtime.getRuntime().gc() //좀더 정확한 값을 알기 위해?? 측정 직전에 GC한다. => 쓸모없음 -> 그래도 일단 추가
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    }

    /** 환경변수를 간단 텍스트 그리드로 */
    fun envToTextGrid(): TextGrid = listOf("name", "value").toTextGrid(System.getenv().map { arrayOf(it.key, it.value) })

    /** JVM 파라메터 출력 */
    fun jvmParam(): List<String> = ManagementFactory.getRuntimeMXBean().inputArguments


}