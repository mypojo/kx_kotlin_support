package net.kotlinx.koin

import org.koin.mp.KoinPlatform


/** 간단 유틸 */
object Koins {

    inline fun <reified T : Any> get() = KoinPlatform.getKoin().get<T>()

}
