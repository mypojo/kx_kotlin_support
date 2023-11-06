package net.kotlinx.koin

import mu.KotlinLogging
import net.kotlinx.core.time.measureTime
import net.kotlinx.module.job.Job
import net.kotlinx.module.job.JobTasklet
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

internal class Koin_성능테스트 : TestRoot(), KoinComponent {

    internal class KoinJob1(val name: String) : JobTasklet {
        private val log = KotlinLogging.logger {}
        override fun doRun(job: Job) {
            log.debug { " -> $name" }
        }
    }

    internal class KoinJob2(val name: String) : JobTasklet {
        private val log = KotlinLogging.logger {}
        override fun doRun(job: Job) {
            log.debug { " -> $name" }
        }
    }

    @Test
    fun `리셋테스트`() {

        load()

        println(getKoin().getAll<JobTasklet>().size)

        load()

        measureTime {
            val jobs = getKoin().getAll<JobTasklet>()
            println(jobs.size)
        }
        measureTime {
            val jobs = getKoin().getAll<JobTasklet>()
            println(jobs.size)
        }
        measureTime {
            val job = getKoin().getAll<JobTasklet>().filterIsInstance<KoinJob1>().firstOrNull { it.name == "테스트-888" }
            println(job!!.name)
        }
        measureTime {
            val job = get<KoinJob1>(named("999"))
            println(job!!.name)
        }


    }

    private fun load() {
        stopKoin()
        startKoin {
            modules(module {
                for (i in 0..1000) {
                    single(named("$i")) { KoinJob1("테스트-$i") } bind JobTasklet::class
                }
                single { KoinJob2("테스트2") }
            })
        }
    }


}