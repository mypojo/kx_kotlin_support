package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.Select
import io.kotest.matchers.shouldBe
import net.kotlinx.aws.AwsClient1
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print


internal class DynamoQuery_쿼리테스트 : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient1>(findProfile97) }

    init {
        log.trace { "실데이터 스캔이라서, 필요할때 켜서 테스트" }
        initTest(KotestUtil.IGNORE)

        Given("DynamoQuery") {

            net.kotlinx.domain.job.Job.TABLE_NAME = "job-dev"

            When("잡 상태 인덱스를 통한 조회") {
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
                val param = net.kotlinx.domain.job.Job("nplCampUpdate01Job", "").apply {
                    jobStatus = JobStatus.SUCCEEDED
                }

                Then("쿼리 실행") {
                    val byStatusPk = DynamoQuery { queryConfig() }
                    val jobs = aws.dynamo.query(byStatusPk, param).datas
                    jobs.print()
                    jobs.size shouldBe byStatusPk.limit
                }

                Then("쿼리를 런타임으로 수정 가능") {
                    var newLimit = 2
                    val jobs = aws.dynamo.query(param) {
                        queryConfig()
                        limit = newLimit
                    }.datas
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


}