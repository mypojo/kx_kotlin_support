package net.kotlinx.aws.cloudfront

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import java.io.File

class CloudfrontUtilTest : BeSpecLight() {


    init {
        initTest(KotestUtil.FAST)

        Given("클라우드 프론트 비용 확인") {
            xThen("클리어 비용 계산") {
                val dir = File("C:\\WORKSPACE\\aa\\bb\\web\\")
                CloudfrontUtil.clearCost(dir)
            }
        }
    }


}
