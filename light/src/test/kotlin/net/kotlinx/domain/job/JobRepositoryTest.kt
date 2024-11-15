package net.kotlinx.domain.job

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.domain.job.define.JobDefinition
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.print
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.hours

class JobRepositoryTest : BeSpecLight() {

    private val jobRepository by koinLazy<JobRepository>(findProfile97)

    class NplKwdReg01Job : JobTasklet {
        override suspend fun execute(job: Job) {}
    }

    val jobDef = JobDefinition {
        jobClass = NplKwdReg01Job::class
    }

    val myName = "0"

    init {
        initTest(KotestUtil.IGNORE)

        Given("카운팅 조회기능") {
            Then("인덱스 - 카운트 조회") {
                val itemCnt = jobRepository.findCntByStatusPk(JobStatus.SUCCEEDED)
                log.info { "카운트 : $itemCnt" }
            }
        }

        Given("기본 조회기능") {

            xThen("단일조회") {
                val job = jobRepository.getItem(Job("kwdDemoMapJob", "58570001"))!!
                listOf(job).print()
            }

            xThen("테스트용 데이터 입력") {
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
                Then("PK 입력시 해당 PK만 출력1") {
                    val lastJobs = jobRepository.findByStatusPk(JobStatus.SUCCEEDED, jobDef) {
                        limit = 4
                    }.datas<Job>()
                    lastJobs.printSimple()
                    lastJobs.size shouldBeGreaterThan 1
                }
                Then("PK 미입력시 전체 PK 출력") {
                    val lastJobs = jobRepository.findByStatusPk(JobStatus.SUCCEEDED) {
                        limit = 6
                    }.datas<Job>()
                    lastJobs.size shouldBeGreaterThan 4
                    lastJobs.printSimple()
                }
                xThen("페이징 없이 전체 쿼리") {
                    val lastJobs = jobRepository.findAllByStatusPk(JobStatus.SUCCEEDED)
                    lastJobs.size shouldBeGreaterThan 4
                    log.info { "전체 크기 ${lastJobs.size}" }
                }
            }

            When("findByPk - PK로 조회") {
                Then("페이징 조회") {
                    val list = jobRepository.findByPk(jobDef) {
                        limit = 2
                    }.datas
                    log.info { "부분 사이즈 : ${list.size}" }
                    list.size shouldBe 2
                }
                xThen("전체 조회 (메모리 주의!!)") {
                    val jobs = jobRepository.findAllByPk(jobDef)
                    log.info { "전체 사이즈 : ${jobs.size}" }
                    jobs.size shouldBeGreaterThan 2
                }
            }

            When("findByMemberId - memberId로 조회") {

                Then("한번에 조회") {
                    val jobs = jobRepository.findByMemberId(jobDef, myName).datas<Job>()
                    jobs.printSimple()
                    jobs.size shouldBeGreaterThan 1
                }
            }


            When("scan - 조건없이 스캔") {

                Then("한번(1m) 스캔") {
                    val jobs = jobRepository.scan()
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
