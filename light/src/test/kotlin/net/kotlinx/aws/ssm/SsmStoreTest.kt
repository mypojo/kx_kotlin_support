package net.kotlinx.aws.ssm

import io.kotest.matchers.shouldNotBe
import net.kotlinx.aws.AwsClient1
import net.kotlinx.kotest.BeSpecLight
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import org.koin.core.component.inject


class SsmStoreTest : BeSpecLight(){

    init {
        initTest(KotestUtil.PROJECT02)

        val aws by inject<AwsClient1>()

        Given("SsmStoreTest") {
            Then("SSM 데이터 로드") {
                val value = aws.ssmStore["/cdk-bootstrap/hnb659fds/version"]
                value shouldNotBe null
                log.info { "SSM bootstrap = $value" }
                "" shouldNotBe ""
            }
        }
    }

}
