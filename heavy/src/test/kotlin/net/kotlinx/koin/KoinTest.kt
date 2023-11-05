package net.kotlinx.koin

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

internal class KoinTest : TestRoot(), KoinComponent {

    class HelloSayer(val name: String) : KoinComponent {
        fun sayHello() = "Hello! $name"

        val helloSayer3: HelloSayer3 by inject()

        init {
            println("생성됨11!!")
        }
    }

    class HelloSayer2(val name: String) {
        fun sayHello() = "Hello! $name"

        init {
            println("생성됨22!!")
            throw IllegalStateException()
        }
    }

    class HelloSayer3(val name: String) : KoinComponent {
        fun sayHello() = "Hello! $name"
    }

    class KoPoo : KoinComponent {

        val helloSayer: HelloSayer by inject()
        val xxx: HelloSayer by inject(named("xxx"))

    }

    @Test
    fun `리셋테스트`() {

        startKoin {
            modules(module {
                single { HelloSayer("테스트1") } bind KoinComponent::class
                single { HelloSayer2("테스트2") }
                single { HelloSayer3("테스트3") } bind KoinComponent::class
                single { HelloSayer("테스트1-오버라이드") }
                single(named("xxx")) { HelloSayer("@_@") }
                single { KoPoo() }
            })
            modules(module {
                single { HelloSayer("테스트1-오버라이드2") }
            })
        }

        val oldPoo = KoPoo()
        println(oldPoo.xxx.sayHello())
        println(oldPoo.helloSayer.sayHello())
        println(oldPoo.helloSayer.helloSayer3.sayHello())

        log.warn { "코인 리셋!!!" }

        println(KoinPlatformTools.defaultContext().getOrNull())
        stopKoin()
        println(KoinPlatformTools.defaultContext().getOrNull())

        try {
            println(KoPoo().helloSayer.sayHello())
        } catch (e: Exception) {
            log.debug { "리셋하면 다시 켜야함.." }
        }

        startKoin {
            val aa = module {
                single { HelloSayer("리셋성공") }
                single { HelloSayer3("테스트 xxx") }
                single { KoPoo() }
            }
            modules(aa)
        }

        println(KoPoo().helloSayer.sayHello())
        println(KoPoo().helloSayer.helloSayer3.sayHello())

        println(oldPoo.helloSayer.sayHello())

        val helloSayer3 = get<HelloSayer3>()
        log.info { "의존성 런타임 가져오기 ${helloSayer3.sayHello()}" }


        val poo: KoPoo by inject()
        println(poo)
        println("===")

    }

    @Test
    fun `리스팅 테스트`() {

        startKoin {
            modules(module {
                single { HelloSayer("테스트1") } bind KoinComponent::class
                single { HelloSayer2("테스트2") }
                single(named("xx")) { HelloSayer3("테스트3") } bind KoinComponent::class
                single(named("yy")) { HelloSayer3("테스트3") } bind KoinComponent::class
            })
        }

        println(getKoin().getAll<KoinComponent>().size)
        println(getKoin().getAll<HelloSayer3>().size)

        println(getKoin().get<KoinComponent>(named("xx")))
        println(getKoin().get<KoinComponent>(named("xx")))
        println(getKoin().get<KoinComponent>(named("yy")))

    }

}