package net.kotlinx.aws.sqs

import com.lectra.koson.obj
import net.kotlinx.concurrent.delay
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.time.toKr01
import kotlin.time.Duration.Companion.seconds

class SqsSupportKtTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("기본기능") {

            val queueName = "${findProfile97}-dlq-dev"

            Then("가시성 체크") {
                repeat(1) {
                    val messages = aws97.sqs.receiveMessageAll(queueName, 1)
                    log.info { "큐 데이터 ${messages.size}건.." }
                    messages.forEach {
                        log.debug { " -> ${it.messageId} from ${it.sentTimestamp.toKr01()}" }
                    }
                    2.seconds.delay()
                }
            }

            Then("DLQ 체크 -> 삭제o") {
                val queueName = "${findProfile97}-dlq-dev"
                val messages = aws97.sqs.receiveMessage(queueName)
                println(messages.size)
                //aws97.sqs.deleteMessageBatch(queueName, messages)
            }
        }

        Given("테스트 입력") {
            Then("fifo큐 입력") {
                val queueName = "${findProfile97}-job-dev.fifo"
                val body = obj {
                    "name" to "test"
                }
                aws97.sqs.sendFifo(queueName, "job-test", body)
            }
        }

    }

}
