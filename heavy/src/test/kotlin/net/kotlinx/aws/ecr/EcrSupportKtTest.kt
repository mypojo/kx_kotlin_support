package net.kotlinx.aws.ecr

import io.kotest.matchers.shouldNotBe
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

internal class EcrSupportKtTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("EcrSupportKt") {
            val aws by koinLazy<AwsClient>()
            val profileName = aws.awsConfig.profileName
            Then("태그로 특정 이미지 조회") {
                val image = aws.ecr.findByTag("$profileName-job", "prod")
                image.imageId shouldNotBe null
            }
        }
    }

}