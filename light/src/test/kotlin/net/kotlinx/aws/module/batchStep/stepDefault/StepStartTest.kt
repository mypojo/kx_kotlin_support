package net.kotlinx.aws.module.batchStep.stepDefault

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.module.batchStep.BatchStepInput
import net.kotlinx.aws.module.batchStep.BatchStepListOption
import net.kotlinx.aws.module.batchStep.BatchStepOption
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.gson.toGsonData
import net.kotlinx.reflect.name
import net.kotlinx.test.MyLightKoinStarter
import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test
import org.koin.core.component.get

class StepStartTest : TestLight() {

    companion object {
        init {
            MyLightKoinStarter.startup("skale")
        }
    }

    @Test
    fun test() {

        val input = BatchStepInput {
            method = StepStart::class.name()
            option = BatchStepOption {
                jobPk = "batchStepTest"
                jobSk = "aaa"
                sfnId = "7dde7369-4d9f-4dd9-a8f2-756d3cfd28ee"
                listOption = BatchStepListOption {
                    waitSeconds = 12
                }
            }
        }

        val runner = get<BatchStepDefaultRunner>()

        runBlocking {
            val result = runner.invoke(input.toJson().toGsonData(), null)!!
            println(GsonData.parse(result).toPreety())
        }


    }


}