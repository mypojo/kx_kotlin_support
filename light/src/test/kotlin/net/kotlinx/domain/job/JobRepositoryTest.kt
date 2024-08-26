package net.kotlinx.domain.job

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.collection.doUntilTokenNull
import net.kotlinx.domain.job.define.JobDefinition
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.hours

class JobRepositoryTest : BeSpecLight() {

    private val jobRepository by koinLazy<JobRepository>(findProfile97)

    class DemoUpdate01Job : JobTasklet {
        override suspend fun doRun(job: Job) {}
    }

    val jobDef = JobDefinition {
        jobClass = DemoUpdate01Job::class
    }

    val myName = "tester"

    init {
        initTest(KotestUtil.PROJECT)

        Given("기본 조회기능") {

            Then("테스트용 데이터 입력") {
                repeat(5) {
                    val job = Job(jobDef.jobPk, "1234${it}") {
                        reqTime = LocalDateTime.now()
                        jobStatus = JobStatus.SUCCEEDED
                        ttl = DynamoUtil.ttlFromNow(1.hours)
                        jobExeFrom = JobExeFrom.ADMIN
                        memberId = myName
                    }
                    jobRepository.putItem(job)
                }
            }

            When("findByStatusPk - 상태로 조회") {
                Then("PK 입력시 해당 PK만 출력") {
                    val lastJobs = jobRepository.findByStatusPk(JobStatus.SUCCEEDED, jobDef) {
                        limit = 4
                    }.datas
                    lastJobs.size shouldBe 4
                }
                Then("PK 미입력시 전체 PK 출력") {
                    val lastJobs = jobRepository.findByStatusPk(JobStatus.SUCCEEDED).datas
                    lastJobs.size shouldBeGreaterThan 4
                    lastJobs.printSimple()
                }
                Then("페이징 없이 전체 쿼리") {
                    val lastJobs = jobRepository.findAllByStatusPk(JobStatus.SUCCEEDED)
                    lastJobs.size shouldBeGreaterThan 4
                    lastJobs.printSimple()
                }
            }

            When("findByPk - PK로 조회") {
                Then("페이징 조회") {
                    val list = jobRepository.findByPk(jobDef) {
                        limit = 2
                    }.datas
                    list.size shouldBe 2
                }
                xThen("전체 조회 (메모리 주의!!)") {
                    val jobs = jobRepository.findAllByPk(jobDef)
                    log.info { "전체 사이즈 : ${jobs.size}" }
                    jobs.size shouldBe 5
                }
            }

            When("findByMemberId - memberId로 조회") {

                Then("한번에 조회") {
                    val jobs = jobRepository.findByMemberId(jobDef, myName)
                    jobs.datas.printSimple()
                    jobs.datas.size shouldBe 5
                }

                Then("동일한걸 페이징으로 나눠서 조회 (jobRepository.findAllByMemberId() 메소드 테스트용)") {
                    val allDatas = doUntilTokenNull { i, last ->
                        log.debug { "[${i}] 페이징 토큰 -> $last" }
                        val jobs = jobRepository.findByMemberId(jobDef, myName, last as Map<String, AttributeValue>?) {
                            this.limit = 2 //너무 커서 테스트가 안되면 숫자를 작게 해서 테스트
                        }
                        jobs.datas.printSimple()
                        jobs.datas.size shouldBeLessThanOrEqual 2
                        jobs.datas to jobs.lastEvaluatedKey
                    }.flatten()
                    log.info { "전체 사이즈 : ${allDatas.size}" }
                }
            }


            xWhen("scan - 조건없이 스캔") {

                Then("한번(1m) 스캔") {
                    val jobs = jobRepository.scan().datas
                    log.info { "결과파일 크기 : ${jobs.size}" }
                }

                Then("전체 스캔") {
                    val jobs = jobRepository.scanAll()
                    log.info { "결과파일 크기 : ${jobs.size}" }
                }

            }


        }
    }

}
