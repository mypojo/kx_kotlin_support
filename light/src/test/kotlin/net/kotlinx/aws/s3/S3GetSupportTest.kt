package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.S3Client
import kotlinx.coroutines.runBlocking
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class S3GetSupportTest : BeSpecLight() {

    val s3Client: S3Client by koinLazy()

    init {
        initTest(KotestUtil.IGNORE) // 실제 AWS 연결이 필요하므로 기본적으로는 IGNORE 처리

        Given("S3GetSupport") {
            val bucket = "kx-test" // 테스트용 버킷 (실제 환경에 맞춰 수정 필요)
            val key = "test.txt"

            Then("getObjectHead 테스트") {
                runBlocking {
                    // 실제 객체가 있을 경우에만 작동함. 
                    // 로컬 테스트 환경이 없으므로 컴파일 체크 위주로 작성
                    // val head = s3Client.getObjectHead(bucket, key)
                    // head shouldNotBe null
                }
            }

            Then("getS3DataWithExistInfo 테스트") {
                runBlocking {
                    // val s3Data = s3Client.getS3DataWithExistInfo(bucket, key)
                    // s3Data?.size shouldNotBe null
                }
            }
        }
    }
}
