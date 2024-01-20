package net.kotlinx.koin

import net.kotlinx.module.job.Job
import net.kotlinx.module.job.JobTasklet
import net.kotlinx.module.job.define.JobDefinition
import net.kotlinx.module.job.define.JobDefinitionUtil
import net.kotlinx.module.job.define.jobReg
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal class KoinJobTest : TestRoot(), KoinComponent {

    class A1 : JobTasklet {
        override fun doRun(job: Job) {
            TODO("Not yet implemented")
        }
    }

    class A2 : JobTasklet {
        override fun doRun(job: Job) {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun `잡 테스트`() {
        startKoin {
            modules(module {
                jobReg {
                    println("xxx1")
                    jobClass = A1::class
                    name = "노션데이터베이스 페이지 -> 구글 캘린더 동기화"
                    comments = listOf(
                        "x분 주기로 동기화",
                        "월비용 =  80원",
                    )
                }
                jobReg {
                    println("xxx2")
                    jobClass = A2::class
                    name = "xxxx"
                    comments = listOf(
                        "yyy",
                    )
                }
            })

            infix fun String.aaa(aa:String):String = aa + "#"




            println("시작")

            val a1: JobDefinition by inject(named("a1"))
            println(a1.name)

            println(JobDefinitionUtil.findById("a2"))
            println(JobDefinitionUtil.findById(A2::class))

            println(getKoin().getAll<JobDefinition>().size)
        }


    }


}