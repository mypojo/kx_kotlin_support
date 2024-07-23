package net.kotlinx.aws.batch

import aws.sdk.kotlin.services.batch.listJobs
import aws.sdk.kotlin.services.batch.model.JobStatus
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print

class BatchSupportKtTest : BeSpecHeavy() {


    init {
        initTest(KotestUtil.IGNORE)

        Given("BatchSupportKt") {

            val aws by koinLazy<AwsClient>(findProfile97)

            xThen("잡 리스팅") {
                aws.batch.listJobs {
                    this.jobQueue = "sin-queue_spot-prod"
                    this.jobStatus = JobStatus.Running
                }.jobSummaryList!!.print()
            }
        }
    }

}