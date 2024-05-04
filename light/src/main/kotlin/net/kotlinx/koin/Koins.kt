package net.kotlinx.koin

import org.koin.mp.KoinPlatform


/**
 * koin은 보조 DI로 만 쓰세요
 *
 * 본격적으로 DI를 사용하려면 다른 제품을 사용하세요
 * https://github.com/kosi-libs/Kodein
 * */
object Koins {

    /**
     * 자동 import 되도록 단순한 단어 사용
     * */
    inline fun <reified T : Any> koin() = KoinPlatform.getKoin().get<T>()


}
