package net.kotlinx.domain.ddb.errorLog

import io.kotest.matchers.shouldBe
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.toErrorLogLink
import net.kotlinx.domain.ddb.DbMultiIndexItemRepository
import net.kotlinx.domain.ddb.DdbBasicRepository
import net.kotlinx.domain.job.Job
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.RandomStringUtil
import net.kotlinx.string.print
import net.kotlinx.time.truncatedMills
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration.Companion.days

class ErrorLogRepositoryTest : BeSpecLight() {

    /** 프로파일 때문에 DI 안함 */
    private val repository by lazy {
        DdbBasicRepository(
            DbMultiIndexItemRepository(findProfile97),
            ErrorLogConverter(),
        )
    }

    init {
        initTest(KotestUtil.PROJECT)

        Given("ErrorLog") {

            val job = Job("demoJob", "1234")

            Then("에러 로그 링크") {
                log.info { "에러로그링크 -> ${job.toErrorLogLink()}" }
            }

            When("단건 테스트") {

                val root = ErrorLog {
                    group = "job"
                    div = job.pk
                    divId = job.sk
                }

                Then("입력 & 조회") {
                    val errorLog = createLog(root)
                    repository.putItem(errorLog)

                    val param = ErrorLog {
                        group = root.group
                        div = root.div
                        divId = root.divId
                        id = errorLog.id
                    }
                    val ddbItem = repository.getItem(param)!!

                    log.debug { "데이터 입력됨. id = ${ddbItem.id}" }
                    ddbItem.time shouldBe errorLog.time
                    ddbItem.stackTrace shouldBe errorLog.stackTrace

                    repository.deleteItem(errorLog)
                }

                Then("대량입력 & 리스팅 & 삭제") {

                    val times = 5
                    repeat(times) {
                        val errorLog = createLog(root)
                        repository.putItem(errorLog)
                    }

                    val param = ErrorLog {
                        group = root.group
                        div = root.div
                        divId = root.divId
                    }
                    val items = repository.findAllBySkPrefix(param).sortedBy { it.time }
                    items.print()
                    items.size shouldBe times

                    items.forEach { repository.deleteItem(it) }
                }
            }

        }


    }

    private fun createLog(root: ErrorLog): ErrorLog = ErrorLog {
        group = root.group
        div = root.div
        divId = root.divId
        id = UUID.randomUUID().toString()
        time = LocalDateTime.now().truncatedMills()
        cause = "테스트 예외 발생"
        stackTrace = RandomStringUtil.getRandomSring(10)
        ttl = DynamoUtil.ttlFromNow(1.days)
    }

}
