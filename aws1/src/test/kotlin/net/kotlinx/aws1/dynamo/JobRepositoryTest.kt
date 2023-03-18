package net.kotlinx.aws1.dynamo

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.aws1.TestRoot
import net.kotlinx.aws1.toAwsClient1
import net.kotlinx.core1.time.toKr01
import org.junit.jupiter.api.Test
import java.time.LocalDateTime


internal class JobRepositoryTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient1()
    val client = aws.dynamo

    @Test
    fun 기본테스트() = runBlocking {

        Job.TABLE_NAME = "job-prod"

        val jobRepository = JobRepository(client)

//        (0..10).forEach {
//            jobRepository.putItem(
//                Job("aa", "id#${it}").apply {
//                    ttl = DynamoUtil.ttlFromNow(HOURS, 2)
//                    jobStatus = "ing"
//                    memberId = "system"
//                    reqTime = LocalDateTime.now()
//                }
//            )
//        }

        val data = Job("aa", "id#3").apply {
            reqTime = LocalDateTime.now().plusYears(1)
            memberId = "system"
            jobStatus = "ok2"
        }

        //업데이트 쿼리 하기
        val updateKeys: List<String> = listOf(Job::reqTime, Job::jobStatus, Job::memberId, Job::memberReqTime).map { it.name }
        jobRepository.updateItem(data, updateKeys)

        val jobs = jobRepository.findLastJobs("aa")
        println(jobs.size)
        jobs.forEach {
            println("${it.pk} / ${it.reqTime?.toKr01()} / ${it.jobStatus}")
        }


    }

}