package net.kotlinx.aws.logs

import aws.sdk.kotlin.services.cloudwatchlogs.getLogEvents
import net.kotlinx.aws.AwsClient
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.system.ResourceHolder
import net.kotlinx.time.toKr01
import net.kotlinx.time.toLong
import java.time.LocalDateTime

class CloudWatchLogsSupportKtTest : BeSpecHeavy() {

    private val profileName by lazy { findProfile97 }
    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.IGNORE)

        xGiven("자주 쓰는 기능") {
            Then("해당 로그그룹을 모두 삭제") {
                val profileName = findProfile97
                val aws = koin<AwsClient>(profileName)
                log.warn { "[/aws/lambda/$profileName-fn-dev] 로그 삭제.." }
                aws.logs.cleanLogStream("/aws/lambda/$profileName-fn-dev")
            }
        }

        Given("CloudWatchLogsSupportKt") {
            xThen("로그 다운로드") {
                var out = ResourceHolder.WORKSPACE.slash("로그다운로드").slash("log.txt")
                aws.logs.download(
                    logGroupName = "/a/b/c",
                    logStreamName = "xxx",
                    out,
                    repeatCnt = 100
                )
                log.warn { "결과파일 ->  ${out.absolutePath}" }
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

            Then("전체 로그 대상으로 쿼리") {
                val doQuery = aws.logs.queryAndWait {
                    this.logGroupNames = listOf("/aws/lambda/adpriv-fn-dev")
                    this.query = "Job"
                    this.startTime = LocalDateTime.now().minusHours(1)
                }

                listOf("시간", "스트림", "링크", "메세지").toTextGridPrint {
                    doQuery.logs.map {
                        arrayOf(
                            it.timestamp.toKr01(),
                            it.logStream,
                            it.logLink,
                            it.message,
                        )
                    }
                }

            }
        }
    }
}