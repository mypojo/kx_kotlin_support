package net.kotlinx.kotest.modules

import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.concurrent.delay
import net.kotlinx.domain.batchTask.BatchTaskExecutor
import net.kotlinx.domain.batchTask.BatchTaskRuntime
import net.kotlinx.domain.batchTask.registBatchTask
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.koin.KoinModule
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

class BatchTaskDemo2 : BatchTaskRuntime {
    override suspend fun executeLogic(option: GsonData, input: List<String>): Map<String, List<GsonData>> {
        1.seconds.delay()
        return mapOf(
            "kwdData2" to listOf(
                obj {
                    "name" to "aaxx"
                    "age" to 10
                }.toGsonData(),
                obj {
                    "name" to "bbcc"
                    "age" to 10
                }.toGsonData(),
            )
        )
    }
}

object BatchTaskModule : KoinModule {

    private val log = KotlinLogging.logger {}

    override fun moduleConfig(): Module = module {

        single { BatchTaskExecutor() }

        registBatchTask("taskDemo1") {
            name = "taskDemo1"
            desc = listOf("demo1 task")
            runtime = object : BatchTaskRuntime {
                override suspend fun executeLogic(option: GsonData, input: List<String>): Map<String, List<GsonData>> {
                    2.seconds.delay()
                    return mapOf(
                        "kwdData1" to listOf(
                            obj {
                                "name" to "이름1"
                                "age" to 10
                            }.toGsonData(),
                            obj {
                                "name" to "이름1"
                                "age" to 10
                            }.toGsonData(),
                        )
                    )
                }
            }
        }

        registBatchTask("taskDemo2") {
            name = "taskDemo2"
            desc = listOf("demo2 task")
            runtime = BatchTaskDemo2()
        }

    }


}

