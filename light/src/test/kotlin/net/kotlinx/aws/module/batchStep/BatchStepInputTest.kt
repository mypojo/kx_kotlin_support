package net.kotlinx.aws.module.batchStep

import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test


class BatchStepInputTest:TestRoot() {

    @Test
    fun test() {
        val input = BatchStepInput().apply {
            retrySfnId = "xxx"
        }
        println(input)
        println(input.toJson())


    }

}