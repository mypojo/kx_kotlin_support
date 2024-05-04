package net.kotlinx.aws

import net.kotlinx.kotest.BeSpecLog
import org.junit.jupiter.api.Test
import java.time.Duration

internal class IamSecretUpdateModuleTest : BeSpecLog() {
    init {
        @Test
        fun `시크릿키 갱신`() {

            val aws = AwsConfig(profileName = "default").toAwsClient() //디폴트로 해야 홀딩 계정으로 접근가능

            val userName = System.getenv("AWS_ID")
            aws.iamSecretUpdateModule.checkAndUpdate(userName, Duration.ofDays(90))

        }
    }
}