package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.Select
import io.kotest.matchers.shouldBe
import net.kotlinx.aws.AwsClient1
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.print


internal class DynamoQuery_쿼리테스트 : BeSpecLight() {

    init {
        initTest(KotestUtil.PROJECT02)

        Given("DynamoQuery") {

            val aws by koinLazy<AwsClient1>()

            net.kotlinx.domain.job.Job.TABLE_NAME = "job-dev"

            /** 쿼리 설정 */
            fun DynamoQuery.queryConfig() {
                indexName = "gidx-jobStatus-pk"
                scanIndexForward = false //최근 데이터 우선
                select = Select.AllProjectedAttributes
                limit = 4
                createParamAndQuery = {
                    val job = it as net.kotlinx.domain.job.Job
                    mapOf(
                        ":${DynamoDbBasic.PK}" to AttributeValue.S(job.pk),
                        ":${net.kotlinx.domain.job.Job::jobStatus.name}" to AttributeValue.S(job.jobStatus!!.name)
                    )
                }
            }

            /** 쿼리 파라메터 */
            val param = net.kotlinx.domain.job.Job("kwdBaseLogic", "").apply {
                jobStatus = JobStatus.SUCCEEDED
            }

            Then("쿼리 -> 고정된 스펙") {
                val byStatusPk = DynamoQuery { queryConfig() }
                val jobs = aws.dynamo.query(byStatusPk, param)
                jobs.print()
                jobs.size shouldBe byStatusPk.limit
            }

            Then("쿼리 -> 런타임 수정") {
                var newLimit = 2
                val jobs = aws.dynamo.query(param) {
                    queryConfig()
                    limit = newLimit
                }
                jobs.print()
                jobs.size shouldBe newLimit
            }

            xThen("전체 쿼리 로드 -> 오래걸림") {
                val byStatusPk = DynamoQuery { queryConfig() }
                aws.dynamo.queryAll(byStatusPk, param)
            }
        }
    }


}