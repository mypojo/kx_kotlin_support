package net.kotlinx.koin

import mu.KotlinLogging
import net.kotlinx.reflect.name
import net.kotlinx.string.print
import net.kotlinx.string.toTextGridPrint
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.error.NoDefinitionFoundException
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
     * kotin 이 없을경우 경고를 같이 해줌
     * 인라인이라 코드 크기 주의해야함!
     * */
    inline fun <reified T : Any> koinOrCheck(qualifier: String? = null, noinline parameters: ParametersDefinition? = null): T {
        return try {
            KoinPlatformTools.defaultContext().get().get<T>(qualifier?.let { named(qualifier) }, parameters)
        } catch (e: NoDefinitionFoundException) {
            qualifier?.let {
                val log = KotlinLogging.logger {}
                log.warn { "[${qualifier}] ${T::class.name()} 로직을 찾을 수 없습니다" }
                koins<T>().print()
            }
            throw e
        }
    }

    /**
     * 자동 import 되도록 단순한 단어 사용
     * */
    inline fun <reified T : Any> koins(): List<T> = KoinPlatformTools.defaultContext().get().getAll<T>()


    /** 없으면 null 리턴 */
    inline fun <reified T : Any> koinOrNull(qualifier: String? = null, noinline parameters: ParametersDefinition? = null): T? {
        return try {
            return koin(qualifier, parameters)
        } catch (e: NoDefinitionFoundException) {
            null
        }
    }


    /**
     * 한번만 시작함 & 재시작 없음
     * ex) 전체 테스트 수행
     * ex) 그래들 스크립트 등
     * synchronized 는 혹시나 해서 넣어줬음
     *  */
    fun startupOnlyOnce(modules: List<Module> = emptyList(), block: Module.() -> Unit = {}) {
        synchronized(this) {
            if (!KoinPlatformTools.exist()) {
                startKoin {
                    modules(
                        modules + module {
                            block()
                        }
                    )
                }
            }
        }
    }

    /**
     * 간단하게 인라인으로 시작 & 재시작함
     * ex) CDK 스크립트
     *  */
    fun startupReset(modules: List<Module> = emptyList(), block: Module.() -> Unit = {}) {
        synchronized(this) {
            stopKoin()
            startKoin {
                modules(
                    modules + module {
                        block()
                    }
                )
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
