package net.kotlinx.aws.dynamo

import net.kotlinx.test.TestRoot


internal class DynamoQuery_쿼리테스트 : TestRoot() {

//    val aws = AwsConfig(profileName = "sin").toAwsClient1()
//
//    val byStatusPk = DynamoQuery {
//        indexName = "gidx-jobStatus-pk"
//        scanIndexForward = false //최근 데이터 우선
//        select = Select.AllProjectedAttributes
//        limit = 4
//        queryParam = {
//            val job = it as Job
//            mapOf(
//                ":${DynamoDbBasic.PK}" to AttributeValue.S(job.pk),
//                ":${Job::jobStatus.name}" to AttributeValue.S(job.jobStatus!!)
//            )
//        }
//    }
//
//    @TestLevel03
//    fun queryAll() = runBlocking {
//
//        Job.TABLE_NAME = "job-prod"
//
//        val param = Job("xxEpJob", "").apply {
//            jobStatus = "SUCCEEDED"
//        }
//        val jobs = aws.dynamo.queryAll(byStatusPk, param)
//        println("jobs.size = ${jobs.size}")
//        jobs.forEach {
//            println("${it.sk}  ")
//        }
//
//    }
//
//    @TestLevel03
//    fun query() = runBlocking {
//
//        Job.TABLE_NAME = "job-prod"
//
//        val param = Job("WConcepMetaEpJob", "").apply {
//            jobStatus = "SUCCEEDED"
//        }
//        val jobs = aws.dynamo.query(byStatusPk, param)
//        println("jobs.size = ${jobs.size}")
//        jobs.forEach {
//            println("${it.sk}  ")
//        }
//
//    }

}