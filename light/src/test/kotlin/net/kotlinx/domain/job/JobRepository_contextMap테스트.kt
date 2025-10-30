package net.kotlinx.domain.job

import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.dynamo.dynamo
import net.kotlinx.aws.dynamo.updateMap
import net.kotlinx.aws.dynamo.updateMapSynch
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.print

class JobRepository_contextMap테스트 : BeSpecLight() {

    private val aws by koinLazy<AwsClient>(findProfile49)

    private val jobRepository by lazy {
        JobRepository().apply {
            this.aws = aws
        }
    }

    init {
        initTest(KotestUtil.IGNORE)

        Given("기본 조회기능") {

            val item = Job("nvRptJob", "10010002")
            Then("단일조회") {
                val job = jobRepository.getItem(item)!!
                listOf(job).print()
            }

            Then("컨텍스트 조회") {
                val job = jobRepository.getItem(item)!!
                println(job.jobContext)
                println(job.jobContextMap)
            }

            Then("컨텍스트 전체 업데이트") {
                val job = jobRepository.getItem(item)!!
                job.jobContextMap = mapOf(
                    "name" to "최불암",
                    "age" to 43,
                    "detail" to mapOf(
                        "v1" to "MD4",
                        "v2" to 78,
                    )
                )
                jobRepository.updateItem(job, JobUpdateSet.CONTEXT)
            }


        }

        Given("로우 API 테스트") {
            Then("컨텍스트 부분 다건 업데이트 (스래드세이프 number add)") {
                val tableName = "job-dev"
                val pk = "nvRptJob"
                val sk = "10010002"
                val mapColumnName = Job::jobContextMap.name

                val newVal = aws.dynamo.updateMapSynch(
                    tableName = tableName,
                    pk = pk,
                    sk = sk,
                    columnName = mapColumnName,
                    mapOf(
                        "age" to 4,
                        "age2" to 5,
                    )
                )
                log.debug { " -> newAge : ${newVal}" }
            }

            Then("컨텍스트 부분 단건 업데이트 (스래드세이프 number add)") {
                val tableName = "job-dev"
                val pk = "nvRptJob"
                val sk = "10010002"
                val mapColumnName = Job::jobContextMap.name
                val mapKey = "age"

                val newVal = aws.dynamo.updateMapSynch(
                    tableName = tableName,
                    pk = pk,
                    sk = sk,
                    columnName = mapColumnName,
                    mapKey = mapKey,
                    incrementValue = 2
                )
                log.debug { " -> newAge : ${newVal}" }
            }

            Then("컨텍스트 부분 다건 업데이트 (문자)") {
                val tableName = "job-dev"
                val pk = "nvRptJob"
                val sk = "10010002"
                val mapColumnName = Job::jobContextMap.name

                val newVal = aws.dynamo.updateMap(
                    tableName = tableName,
                    pk = pk,
                    sk = sk,
                    columnName = mapColumnName,
                    mapOf(
                        "name" to "불암형님v2",
                        "name2" to "닥터5",
                    )
                )
                log.debug { " -> newAge : ${newVal}" }
            }
        }
    }

}
