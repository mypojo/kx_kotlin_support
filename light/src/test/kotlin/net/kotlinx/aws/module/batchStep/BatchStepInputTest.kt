package net.kotlinx.aws.module.batchStep

import net.kotlinx.aws.module.batchStep.stepDefault.StepStart
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import java.util.*


class BatchStepInputTest : TestRoot() {

    @Test
    fun test() {

        val input = BatchStepInput {
            method = StepStart::class.simpleName
            option = BatchStepOption {
                jobPk = this::class.simpleName!!
                sfnId = UUID.randomUUID().toString()
                listOption = BatchStepListOption {
                    waitSeconds = 12
                }
            }
        }
        val json = input.toJson()
        println(json)
        val vo = BatchStepInput.parseJson(json)

        check(vo.method == StepStart::class.simpleName)
        check(vo.option.listOption?.waitSeconds == 12)


    }

}