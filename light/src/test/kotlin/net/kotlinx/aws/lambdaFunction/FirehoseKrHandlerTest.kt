package net.kotlinx.aws.lambdaFunction

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification
import io.mockk.mockk
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

internal class FirehoseKrHandlerTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("FirehoseKrHandler") {
            xThen("실 사용시 CommonFunctionHandler 으로 사용해야함") {
                val firehoseKr = FirehoseKrHandler()

                val s3Event = S3Event(
                    listOf(
                        S3EventNotification.S3EventNotificationRecord(
                            "",
                            "",
                            "",
                            null,
                            "",
                            null,
                            null,
                            S3EventNotification.S3Entity(
                                null,
                                S3EventNotification.S3BucketEntity("bucketName", null, null),
                                S3EventNotification.S3ObjectEntity(
                                    "collect/event_kotlinx/basicDate=20220926/hh=20/event_event_kotlinx-1-2022-09-26-02-48-25-9482ca1e-a020-4caa-8282-9d758985e5d1",
                                    null,
                                    null,
                                    null,
                                    null,
                                ),
                                null,
                            ),
                            null,
                        )
                    )
                )
                val context = mockk<Context>()
                firehoseKr.handleRequest(s3Event, context)
            }
        }
    }
}