package net.kotlinx.aws.s3

import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsConfig
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

internal class S3PresignSupportTest : BeSpecLight() {

    init {
        initTest(KotestUtil.PROJECT02)

        Given("S3SupportKt") {
            val aws: AwsClient1 = koin()
            val profileName = koin<AwsConfig>().profileName!!

            Then("버킷 프리사인_다운로드URL 생성") {
                val url = aws.s3.presignGetObject("$profileName-work-dev", "code/$profileName-layer_v1-dev/deployLayerV1.zip")
                log.debug { "presignGetObject url : $url" }
            }
            Then("프리사인_업로드URL 생성") {
                val url = aws.s3.presignPutObject("$profileName-work-dev", "code/$profileName-layer_v1-dev/")
                log.debug { "presignPutObject url : $url" }
            }

        }
    }


}
