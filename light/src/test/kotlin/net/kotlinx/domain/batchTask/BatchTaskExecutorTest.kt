package net.kotlinx.domain.batchTask

import com.lectra.koson.arr
import com.lectra.koson.obj
import io.kotest.matchers.shouldBe
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicInput
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class BatchTaskExecutorTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.FAST)

        Given("BatchTaskExecutor") {

            val executor = BatchTaskExecutor()

            val option = obj {
                BatchTaskOptionUtil.BATCH_TASK_IDS to arr[
                    "taskDemo1",
                    "taskDemo2",
                ]
            }
            val datas = listOf(
                "청바지", "냉장고", "마우스"
            )

            Then("데모 테스트 -> map") {
                val result = executor.executeLogic(option.toGsonData(), datas)
                result.entries.size shouldBe 2
            }

            Then("데모 테스트 -> json") {
                val input = S3LogicInput(
                    "batchTask",
                    datas,
                    option.toString()
                )
                val result = executor.executeLogic(input)
                log.debug { "결과 json -> ${result.result}" }
            }
        }
    }

}
