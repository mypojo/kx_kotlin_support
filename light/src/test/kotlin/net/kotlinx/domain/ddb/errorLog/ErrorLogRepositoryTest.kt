package net.kotlinx.domain.ddb.errorLog

import io.kotest.matchers.shouldBe
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.domain.item.errorLog.ErrorLog
import net.kotlinx.domain.item.errorLog.ErrorLogRepository
import net.kotlinx.domain.item.errorLog.errorLogQueryLink
import net.kotlinx.domain.job.Job
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.RandomStringUtil
import net.kotlinx.string.print
import net.kotlinx.time.truncatedMills
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration.Companion.minutes

class ErrorLogRepositoryTest : BeSpecLight() {

    init {
        initTest(KotestUtil.PROJECT)

        Given("ErrorLog") {

            val repository = ErrorLogRepository().apply {
                aws = koin<AwsClient>(findProfile97)
            }

            val job = Job("kwdDemoListJob", "59110001")
            Then("에러 로그 링크") {
                log.info { "에러로그링크 -> ${job.errorLogQueryLink}" }
            }


            When("단건 테스트") {

                val errorLog = ErrorLog(
                    group = "job",
                    div = "kwdDemoListJob",
                    divId = "59110001",
                    id = UUID.randomUUID().toString(),
                    time = LocalDateTime.now().truncatedMills(),
                    cause = "테스트 예외 발생",
                    stackTrace = RandomStringUtil.getRandomSring(10),
                    ttl = DynamoUtil.ttlFromNow(10.minutes),
                )

                Then("카운팅") {
                    val exist = repository.findCnt(errorLog.group, errorLog.div)
                    repository.putItem(errorLog)
                    val now = repository.findCnt(errorLog.group, errorLog.div)
                    log.debug { " 에러로그 카운팅 ${exist} -> ${now}" }
                    now shouldBe exist + 1
                }

                Then("입력 & 조회") {
                    repository.putItem(errorLog)
                    val logs = repository.findAll(errorLog.group, errorLog.div)
                    logs.print()

                    logs.first { it.id == errorLog.id }.cause shouldBe errorLog.cause

                    repository.deleteItem(errorLog)
                }

            }

        }


    }


}
