package net.kotlinx.aws.s3

import io.kotest.matchers.shouldBe
import net.kotlinx.aws.AwsClient1
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.okhttp.fetch
import net.kotlinx.system.ResourceHolder
import okhttp3.OkHttpClient

internal class S3PresignSupportTest : BeSpecHeavy() {

    private val profileName by lazy { findProfile28 }
    private val aws by lazy { koin<AwsClient1>(profileName) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("S3SupportKt") {

            Then("버킷 프리사인_다운로드URL 생성") {
                val url = aws.s3.presignGetObject("$profileName-work-dev", "code/$profileName-layer_v1-dev/deployLayerV1.zip")
                log.debug { "presignGetObject url : $url" }
            }

            Then("버킷 프리사인_다운로드URL 생성 (이름변경)") {
                val url = aws.s3.presignGetObjectUrl {
                    bucket = "$profileName-work-dev"
                    key = "code/$profileName-layer_v1-dev/deployLayerV1.zip"
                    downloadName = "커스텀V2 데이터 SM.zip"
                }
                log.debug { "presignGetObject url : $url" }
            }

            Then("프리사인_업로드") {
                val file = ResourceHolder.WORKSPACE.slash("input.csv")
                if(!file.exists()){
                    file.writeText("test")
                }
                val metadata = mapOf(
                    "type" to "test1",
                    "name" to "영감님Mk2",
                    "div" to "타입^^;",
                )
                val s3Obj = S3Data("$profileName-work-dev", "upload/presignPutObject/${file.name}")
                val presign = aws.s3.presignPutObject(s3Obj.bucket, s3Obj.key, metadata = metadata)

                val client = koin<OkHttpClient>()
                val resp = client.fetch {
                    url = presign.url.toString()
                    method = presign.method.name
                    header = presign.headerMap
                    body = file
                }

                resp.response.code shouldBe 200

                val objMetadata = aws.s3.getObjectMetadata(s3Obj.bucket, s3Obj.key)
                objMetadata["div"] shouldBe "타입^^;"

            }

        }
    }


}
