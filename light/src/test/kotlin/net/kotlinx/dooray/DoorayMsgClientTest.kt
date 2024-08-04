package net.kotlinx.dooray

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class DoorayMsgClientTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("두레이 알림훅 테스트") {
            Then("일만 메세지 전송") {
                val doorayMsgClient = DoorayMsgClient {
                    name = "멍멍이"
                    roomUri = "https://hook.dooray.com/services/1562821692791377779/3860203519904797998/iyAh29JbQT6ILx6dPed03A"
                }
                doorayMsgClient.sendDirect("멍멍테스트 https://naver.com")
            }
        }
    }

}
