package net.kotlinx.core1.lib

import java.net.Inet4Address


object SystemUtil {

    val ip: String by lazy { Inet4Address.getLocalHost().hostAddress }

    fun nowUsedMemory(): Long {
        Runtime.getRuntime().gc() //좀더 정확한 값을 알기 위해?? 측정 직전에 GC한다. => 쓸모없음 -> 그래도 일단 추가
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    }

}