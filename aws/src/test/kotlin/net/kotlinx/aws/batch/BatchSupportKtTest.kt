package net.kotlinx.aws.batch

import aws.sdk.kotlin.services.batch.listJobs
import aws.sdk.kotlin.services.batch.model.JobStatus
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.core2.test.TestLevel03
import net.kotlinx.core2.test.TestRoot

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