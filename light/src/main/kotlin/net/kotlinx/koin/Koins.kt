package net.kotlinx.koin

import mu.KotlinLogging
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.mp.KoinPlatform


/**
 * koin은 보조 DI로 만 쓰세요
 * 기존 로드법이, 특정 인터페이스를 사용해야 하거나 네이밍이 맘에 안들어서 새로 만듬
 *
 * 본격적으로 DI를 사용하려면 다른 제품을 사용하세요
 * https://github.com/kosi-libs/Kodein
 * */
object Koins {

    private val log = KotlinLogging.logger {}

    /**
     * 자동 import 되도록 단순한 단어 사용
     * */
    inline fun <reified T : Any> koin(qualifier: String? = null): T {
        return when (qualifier) {
            null -> KoinPlatform.getKoin().get<T>()
            else -> KoinPlatform.getKoin().get<T>(named(qualifier))
        }
    }

    /**
     * 자동 import 되도록 단순한 단어 사용
     * */
    inline fun <reified T : Any> koins(): List<T> = KoinPlatform.getKoin().getAll<T>()

    /**
     * koin 시작
     * ex) Koins.startup(BeSpecLight.MODULES)
     * */
    fun startup(modules: List<Module>) {
        log.info { "koin startup .." }
        stopKoin()
        startKoin {
            modules(modules)
        }
    }


}
