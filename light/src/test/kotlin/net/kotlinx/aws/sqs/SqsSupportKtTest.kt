package net.kotlinx.aws.sqs

import net.kotlinx.concurrent.delay
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.capital
import net.kotlinx.time.toKr01
import kotlin.time.Duration.Companion.seconds

class SqsSupportKtTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("기본기능") {

            val queueName = "${findProfile97}-dlq-dev"

            Then("asd") {
                println("NV".capital())
                println("NV".capitalize())
            }

            Then("가시성 체크") {
                repeat(1) {
                    log.info { "큐 데이터 로드.." }
                    //val messages = aws97.sqs.receiveMessageAll(queueName, 30)
                    val messages = aws97.sqs.receiveMessage(queueName, visibilityTimeout = 30, waitTimeSeconds = 0)
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

    }

}