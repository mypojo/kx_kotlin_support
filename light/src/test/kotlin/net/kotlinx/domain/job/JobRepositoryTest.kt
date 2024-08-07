package net.kotlinx.domain.job

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import io.kotest.matchers.ints.shouldBeLessThan
import net.kotlinx.collection.doUntilTokenNull
import net.kotlinx.domain.job.define.JobDefinition
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class JobRepositoryTest : BeSpecLight() {

    private val jobRepository by koinLazy<JobRepository>(findProfile97())

    init {
        initTest(KotestUtil.PROJECT)

        Given("기본 조회기능") {

            class NplCampUpdate01Job : JobTasklet {
                override fun doRun(job: Job) {}
            }

            val jobDef = JobDefinition {
                jobClass = NplCampUpdate01Job::class
            }

            When("findByStatusPk - 상태로 조회") {
                Then("PK 입력시 해당 PK만 출력") {
                    val lastJobs = jobRepository.findByStatusPk(JobStatus.SUCCEEDED, jobDef) {
                        limit = 4
                    }.datas
                    lastJobs.map { it.pk }.distinct().size shouldBeLessThan 2
                    lastJobs.printSimple()
                }
                Then("PK 미입력시 전체 PK 출력") {
                    val lastJobs = jobRepository.findByStatusPk(JobStatus.SUCCEEDED).datas
                    lastJobs.printSimple()
                }
            }

            When("findByPk - PK로 조회") {
                Then("페이징 조회") {
                    jobRepository.findByPk(jobDef) {
                        limit = 2
                    }.datas.printSimple()
                }
                xThen("전체 조회 (메모리 주의!!)") {
                    val jobs = jobRepository.findAllByPk(jobDef)
                    log.info { "전체 사이즈 : ${jobs.size}" }
                }
            }

            When("findByMemberId - memberId로 조회") {

                Then("한번에 조회") {
                    val jobs = jobRepository.findByMemberId(jobDef, "test1")
                    jobs.datas.printSimple()
                }

                Then("동일한걸 페이징으로 나눠서 조회") {
                    val allDatas = doUntilTokenNull { i, last ->
                        log.debug { "[${i}] 페이징 토큰 -> $last" }
                        val jobs = jobRepository.findByMemberId(jobDef, "test1", last as Map<String, AttributeValue>?) {
                            this.limit = 2 //너무 커서 테스트가 안되면 숫자를 작게 해서 테스트
                        }
                        jobs.datas.printSimple()
                        jobs.datas to jobs.lastEvaluatedKey
                    }.flatten()
                    log.info { "전체 사이즈 : ${allDatas.size}" }
                }
            }


            When("scan - 조건없이 스캔") {

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
