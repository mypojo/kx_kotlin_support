package net.kotlinx.aws.iam

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import java.time.Duration

class IamSecretUpdateModuleTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("iamSecretUpdateModule") {
            log.warn { "시크릿 파일이 변경될 수 있음!! 주의!!" }
            Then("시크릿키 갱신") {
                val aws by koinLazy<AwsClient>()
                val userName = System.getenv("AWS_ID")
                aws.iamSecretUpdateModule.checkAndUpdate(userName, Duration.ofDays(19))
            }
        }
    }

}
