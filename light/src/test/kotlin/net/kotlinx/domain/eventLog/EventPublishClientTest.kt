package net.kotlinx.domain.eventLog

import com.lectra.koson.obj
import net.kotlinx.aws.firehose.firehose
import net.kotlinx.aws.firehose.putRecord
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class EventPublishClientTest : BeSpecLight(){

    init {
        initTest(KotestUtil.IGNORE)

        Given("FirehoseLogger") {

            Then("일반호출 테스트") {
                val obj = obj {
                    "id" to "#1"
                }
                aws97.firehose.putRecord("demo-s3", obj.toString())
            }

        }
    }

}
