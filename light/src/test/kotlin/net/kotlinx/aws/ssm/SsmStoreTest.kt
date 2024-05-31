package net.kotlinx.aws.ssm

import io.kotest.matchers.shouldNotBe
import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight


class SsmStoreTest : BeSpecLight() {

    private val profileName by lazy { findProfile28() }
    private val aws by lazy { koin<AwsClient1>(profileName) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("SsmStoreTest") {
            Then("SSM 데이터 로드") {
                val value = aws.ssmStore["/cdk-bootstrap/hnb659fds/version"]
                value shouldNotBe null
                log.info { "SSM bootstrap = $value" }
            }
        }
    }

}
