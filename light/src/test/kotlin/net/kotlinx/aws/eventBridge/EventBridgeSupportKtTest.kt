package net.kotlinx.aws.eventBridge

import com.lectra.koson.obj
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class EventBridgeSupportKtTest : BeSpecHeavy() {


    init {
        initTest(KotestUtil.IGNORE)

        Given("커스텀 이벤트 전달") {

            Then("SNS") {

                val config = EventBridgeConfig(
                    "${findProfile97}-dev",
                    "${findProfile97}.test",
                    "test01",
                )

                val obj = obj {
                    "name" to "SNS 테스트"
                }
                aws97.event.putEvents(config, listOf(obj.toGsonData().toString()))
            }

            Then("람다") {

                val config = EventBridgeConfig(
                    "${findProfile97}-dev",
                    "${findProfile97}.lambda",
                    "test01",
                )

                val obj = obj {
                    "name" to "람다 테스트"
                }
                aws97.event.putEvents(config, listOf(obj.toGsonData().toString()))
            }


        }
    }


}
