package net.kotlinx.aws

import net.kotlinx.TestRoot
import net.kotlinx.aws1.AwsConfig
import org.junit.jupiter.api.Test
import java.time.Duration

internal class AwsIamModuleTest:TestRoot(){

    @Test
    fun `시크릿키 갱신`(){

        val aws = AwsConfig(profileName = "default").toAwsClient() //디폴트로 해야 홀딩 계정으로 접근가능

        val userName = System.getenv("AWS_ID")
        aws.iamModule.changeLocalSecretKey(userName, Duration.ofDays(90))

    }
}