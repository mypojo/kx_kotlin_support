package net.kotlinx.aws.logs

import aws.sdk.kotlin.services.cloudwatchlogs.getLogEvents
import net.kotlinx.aws.AwsClient
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print
import net.kotlinx.string.toTextGrid
import net.kotlinx.system.ResourceHolder
import net.kotlinx.time.toLong
import java.time.LocalDateTime

class CloudWatchLogsSupportKtTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.PROJECT02)

        Given("CloudWatchLogsSupportKt") {
            val aws = koin<AwsClient>()
            xThen("로그 다운로드") {
                var out = ResourceHolder.getWorkspace().slash("로그다운로드").slash("log.txt")
                aws.logs.download(
                    logGroupName = "/a/b/c",
                    logStreamName = "xxx",
                    out,
                    repeatCnt = 100
                )
                log.warn { "결과파일 ->  ${out.absolutePath}" }
            }

            xThen("해당 로그그룹을 모두 삭제") {
                aws.logs.cleanLogStream("/aws/lambda/xx-fn-dev")
            }

            xThen("로그스트림 내부의 로그(이벤트) 조회") {
                val logs = aws.logs.getLogEvents {
                    this.logGroupName = "/aws/ecs/web-prod"
                    this.logStreamName = "sin-web/sin-web_container-prod/31d271dc4ae147c4b570317a60252ae7"
                    this.limit = 4
                    this.startTime = LocalDateTime.now().minusHours(16).toLong()  //디폴트 한국시간임
                    this.endTime = LocalDateTime.now().minusHours(1).toLong()
                }.events!!
                logs.print()
            }

            xThen("전체 로그 대상으로 쿼리") {
                val doQuery = aws.logs.queryAndWait {
                    this.logGroupNames = listOf("/aws/lambda/aa/-fn-dev")
                    this.query = "WAS lambda 과금"
                    this.startTime = LocalDateTime.now().minusHours(1)
                }
                listOf("메시지", "링크").toTextGrid(doQuery.map { arrayOf(it.message, it.toLogLink()) }).print()
            }
        }
    }
}