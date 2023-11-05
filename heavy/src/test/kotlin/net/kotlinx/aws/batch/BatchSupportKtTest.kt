package net.kotlinx.aws.batch

import aws.sdk.kotlin.services.batch.listJobs
import aws.sdk.kotlin.services.batch.model.JobStatus
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.test.TestLevel03
import net.kotlinx.test.TestRoot

class BatchSupportKtTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

    @TestLevel03
    fun test() {

        runBlocking {
            aws.batch.listJobs {
                this.jobQueue = "sin-queue_spot-prod"
                this.jobStatus = JobStatus.Running
            }.jobSummaryList!!.forEach {
                println(it)
            }
        }

    }

}