package net.kotlinx.domain.batchStep.stepDefault

import net.kotlinx.aws.lambda.dispatch.synch.BatchStepDispatcher
import net.kotlinx.domain.batchStep.BatchStepListOption
import net.kotlinx.domain.batchStep.BatchStepMode
import net.kotlinx.domain.batchStep.BatchStepOption
import net.kotlinx.domain.batchStep.BatchStepParameter
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.reflect.name

class BatchStepDefaultRunnerTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("BatchStepDefaultRunner") {

            val runner by koinLazy<BatchStepDispatcher>()
            Then("StepStart 테스트") {

                val input = BatchStepParameter {
                    method = StepStart::class.name()
                    option = BatchStepOption {
                        jobPk = "batchStepTest"
                        jobSk = "aaa"
                        sfnId = "7dde7369-4d9f-4dd9-a8f2-756d3cfd28ee"
                        listOption = BatchStepListOption {
                            waitSeconds02 = 12
                        }
                        mode = BatchStepMode.MAP_INLINE
                    }
                }

                val result = runner.postOrSkip(input.toJson().toGsonData(), null)!!
                println(GsonData.parse(result).toPreety())

            }
            Then("StepEnd 테스트") {

                val input = BatchStepParameter {
                    method = StepEnd::class.name()
                    option = BatchStepOption {
                        jobPk = "batchStepTest"
                        jobSk = "aaa"
                        sfnId = "7dde7369-4d9f-4dd9-a8f2-756d3cfd28ee"
                        listOption = BatchStepListOption {
                            waitSeconds02 = 12
                        }
                    }
                }

                val result = runner.postOrSkip(input.toJson().toGsonData(), null)!!
                println(result)
                println(GsonData.parse(result).toPreety())
            }
        }
    }


}