package net.kotlinx.koin

import org.koin.mp.KoinPlatform


object Koins {

    inline fun <reified T : Any> get() = KoinPlatform.getKoin().get<T>()

}
