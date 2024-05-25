package net.kotlinx.gradle

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.system.SystemUtil

class SystemUtilTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("SystemUtil") {
            Then("환경변수 출력") {
                printName()
                SystemUtil.envPrint()
            }
            Then("시스템 프로퍼티 출력 (커스텀 불가능)") {
                printName()
                SystemUtil.systemPropertyPrint()
            }
            Then("JVM 파라메터 출력") {
                printName()
                SystemUtil.jvmParamPrint()
            }
        }
    }

}