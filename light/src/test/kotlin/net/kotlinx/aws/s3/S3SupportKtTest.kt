package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.deleteObject
import aws.sdk.kotlin.services.s3.listBuckets
import ch.qos.logback.classic.Level
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import net.kotlinx.aws.AwsClient
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.logback.LogBackUtil
import net.kotlinx.string.print
import net.kotlinx.system.ResourceHolder

internal class S3SupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("S3SupportKt") {

            val profile = findProfile97

            Then("대용량 CSV 스트리밍 읽기") {
                val aws2 by lazy { koin<AwsClient>(findProfile48) }
                aws2.s3.getObjectLinesStream("nak-real-work", "config/nak/ssg/ep/ssg_metaEp.csv") { flow ->
                    flow.take(10 + 1).toList().print()
                }
            }

            Then("대용량 CSV 스트리밍 읽기 - 조건문 사용") {
                val aws2 by lazy { koin<AwsClient>(findProfile48) }
                aws2.s3.getObjectLinesStream("nak-real-work", "config/nak/ssg/ep/ssg_metaEp.csv") { flow ->
                    var cnt = 0
                    flow.takeWhile {
                        ++cnt < 6
                    }.toList().print()
                }
            }

            Then("대용량 CSV 스트리밍 읽기 - 조건문 사용 & 청크 읽기") {
                val aws2 by lazy { koin<AwsClient>(findProfile48) }
                aws2.s3.getObjectLinesStream("nak-real-work", "config/nak/ssg/ep/ssg_metaEp.csv") { flow ->
                    var cnt = 0
                    flow.takeWhile {
                        ++cnt < 6
                    }.chunked(4) .collect {
                        println(it)
                    }
                }
            }



            Then("버킷 리스팅") {
                val buckets = aws.s3.listBuckets {}.buckets!!
                buckets.size shouldBeGreaterThan 0
                buckets!!.print()
            }

            Then("페이징읽기") {
                val files = aws.s3.listObjects("$profile-work-dev", "code/")
                files.size shouldBeGreaterThan 0
                files.print()
            }

            Then("디렉토링") {
                val files = aws.s3.listDirs("$profile-work-dev", "work/")
                files.size shouldBeGreaterThan 0
                files.print()
            }

            When("메타데이터") {
                val file = ResourceHolder.WORKSPACE.slash("input.txt")
                val s3Data = S3Data("$profile-work-dev", "upload/input.txt")

                Then("메타데이터 추가 업로드") {
                    file.writeText("영감님 멍멍")
                    aws.s3.putObject(
                        s3Data.bucket, s3Data.key, file, mapOf(
                            "aa" to "bb",
                            "cc" to "7788",
                            "fileName" to "영감님ab12만세"
                        )
                    )
                }
                Then("메타데이터 읽기 - key는 소문자로 저장된다") {
                    val metadata = aws.s3.getObjectMetadata(s3Data.bucket, s3Data.key)!!
                    metadata["fileName"] shouldBe null
                    metadata["filename"] shouldBe "영감님ab12만세"
                }
                Then("데이터 정리") {
                    aws.s3.deleteObject {
                        bucket = s3Data.bucket
                        key = s3Data.key
                    }
                }
            }

            xThen("메타데이터 읽기 2") {
                val metadata = aws.s3.getObjectMetadata("$profile-work-dev", "work/job/nplKwdDown01Job/20050001/OUTPUT.csv")!!
                println(metadata)
            }

            xThen("멀티파트 업로드") {
                LogBackUtil.logLevelTo(testClassName, Level.TRACE)
                val file = ResourceHolder.WORKSPACE.slash("aa").slash("bb-202405.csv.zip")
                aws.s3.putObjectMultipart("$profile-work-dev", "upload/temp.csv", file, 100)
            }
        }
    }

}