package net.kotlinx.aws.iam

import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsLocal
import net.kotlinx.koin.Koins.koin
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
            val aws by koinLazy<AwsClient>()

            Then("억세스 체크") {
                println(aws.awsConfig.callerIdentity.arn)
                println(koin<AwsClient>(findProfile97).awsConfig.callerIdentity.arn)
            }

            Then("시크릿키 갱신") {
                println(AwsLocal.AWS_USER_NAME)
                IamSecretUpdateModule().checkAndUpdate(Duration.ofDays(30))
            }
        }
    }

}
