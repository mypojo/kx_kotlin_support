package net.kotlinx.aws.sqs

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class SqsSupportKtTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("기본기능") {
            Then("DLQ 체크 -> 삭제o") {

                val queueName = "${findProfile97}-dlq-dev"
                val messages = aws97.sqs.receiveMessage(queueName)

                aws97.sqs.deleteMessageBatch(queueName, messages)

            }
        }
    }

}
