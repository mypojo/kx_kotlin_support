package net.kotlinx.koin

import mu.KotlinLogging
import net.kotlinx.string.toTextGridPrint
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools


/**
 * 기존 로드법이, KoinComponent 인터페이스를 사용해야 하거나 호출 네이밍이 겹처서 새로 만듬
 * Global 에만 적용됨
 *
 * 장점 : 런타임에 적용됨
 * 단점 : 런타임에 적용됨..
 *
 * koin은 보조 DI로 만 쓰세요.
 * 기
 *
 * 본격적으로 DI를 사용하려면 다른 제품을 사용하세요
 * https://github.com/kosi-libs/Kodein
 * */
object Koins {

    private val log = KotlinLogging.logger {}

    /**
     * 자동 import 되도록 단순한 단어 사용
     *  */
    inline fun <reified T : Any> koinLazy(qualifier: String? = null, noinline parameters: ParametersDefinition? = null): Lazy<T> =
        lazy(KoinPlatformTools.defaultLazyMode()) { koin<T>(qualifier, parameters) }

    /**
     * 자동 import 되도록 단순한 단어 사용
     * 이거는 즉시 로드함.
     * 지연 로딩을 사용하고싶은경우 by inject() 를 사용할것
     * */
    inline fun <reified T : Any> koin(qualifier: String? = null, noinline parameters: ParametersDefinition? = null): T =
        KoinPlatformTools.defaultContext().get().get<T>(qualifier?.let { named(qualifier) }, parameters)

    /**
     * 자동 import 되도록 단순한 단어 사용
     * */
    inline fun <reified T : Any> koins(): List<T> = KoinPlatformTools.defaultContext().get().getAll<T>()

    /**
     * koin 시작
     * ex) Koins.startup(BeSpecLight.MODULES)
     * */
    fun startup(modules: List<Module>) {
        stopKoin()
        startKoin {
            modules(modules)
        }
    }


    /**
     * 간단하게 인라인으로 시작
     * ex) 그래들 스크립트 등
     *  */
    fun startup(block: Module.() -> Unit) {
        if (!KoinPlatformTools.exist()) {
            startKoin {
                modules(module {
                    block()
                })
            }
        }
    }

    /** 파라메터가 있어야 가져올 수 있는것도 있음 주의! */
    @OptIn(KoinInternalApi::class)
    fun printAll() {
        val instances = GlobalContext.get().instanceRegistry.instances
        listOf("class", "named").toTextGridPrint {
            instances.keys.sorted().map { k ->
                //net.kotlinx.aws.module.batchStep.BatchStepExecutor::_root_
                val value = k.removeSuffix(":_root_").removeSuffix(":").split(":")
                arrayOf(value[0], value.getOrNull(1) ?: "")
            }
        }
    }
}
