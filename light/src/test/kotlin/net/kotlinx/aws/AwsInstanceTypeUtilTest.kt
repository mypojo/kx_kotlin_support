package net.kotlinx.aws

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class AwsInstanceTypeUtilTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("AwsInstanceTypeUtil") {
            Then("로컬로 표기되어야함") {
                AwsInstanceTypeUtil.INSTANCE_TYPE shouldBe AwsInstanceType.LOCAL
            }
        }
    }

}