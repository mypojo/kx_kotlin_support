package net.kotlinx.aws.module.batchStep.stepDefault

import net.kotlinx.aws.module.batchStep.BatchStepInput
import net.kotlinx.aws.module.batchStep.BatchStepListOption
import net.kotlinx.aws.module.batchStep.BatchStepMode
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.reflect.name

class BatchStepDefaultRunnerTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("BatchStepDefaultRunner") {

            val runner = koin<BatchStepDefaultRunner>()
            Then("StepStart 테스트") {

                val input = BatchStepInput(StepStart::class.name()) {
                    jobPk = "batchStepTest"
                    jobSk = "aaa"
                    sfnId = "7dde7369-4d9f-4dd9-a8f2-756d3cfd28ee"
                    listOption = BatchStepListOption {
                        waitSeconds = 12
                    }
                    mode = BatchStepMode.MAP_INLINE
                }

                val result = runner.invoke(input.toJson().toGsonData(), null)!!
                println(GsonData.parse(result).toPreety())

            }
            Then("StepEnd 테스트") {

                val input = BatchStepInput(StepEnd::class.name()) {
                    jobPk = "batchStepTest"
                    jobSk = "aaa"
                    sfnId = "7dde7369-4d9f-4dd9-a8f2-756d3cfd28ee"
                    listOption = BatchStepListOption {
                        waitSeconds = 12
                    }
                }

                val result = runner.invoke(input.toJson().toGsonData(), null)!!
                println(result)
                println(GsonData.parse(result).toPreety())
            }
        }
    }


}