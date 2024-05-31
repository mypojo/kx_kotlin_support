package net.kotlinx.aws.s3

import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

internal class S3PresignSupportTest : BeSpecLight() {

    private val profileName by lazy { findProfile28() }
    private val aws by lazy { koin<AwsClient1>(profileName) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("S3SupportKt") {

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
