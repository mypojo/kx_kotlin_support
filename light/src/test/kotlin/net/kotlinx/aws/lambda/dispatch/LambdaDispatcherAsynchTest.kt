package net.kotlinx.aws.lambda.dispatch

import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class LambdaDispatcherAsynchTest : BeSpecHeavy() {

    private val dispatcher by koinLazy<LambdaDispatcher>()

    init {
        initTest(KotestUtil.IGNORE)

        Given("SchedulerEventPublisher 스케줄링") {
            val input = obj {
                //SchedulerEventPublisher.DETAIL_TYPE to SchedulerEventPublisher.SCHEDULED_EVENT
                "resources" to arr[
                    "arn:aws:scheduler:ap-northeast-2:99999999:schedule/newGroup/demoJob"
                ]
            }

            Then("설정된 스케쥴링으로 이벤트 수신") {
                dispatcher.handleRequest(input)
            }
        }

        Given("JOB 이벤트 수신") {
            val input = obj {
                "detail-type" to "Job Status Change"
                "source" to "kotlinx.job"
                "detail" to obj {
                    "pk" to "demoJob"
                    "sk" to "12345"
                    "memberId" to "system"
                    "ttl" to 0
                    "jobStatus" to "SUCCEEDED"
                    "jobErrMsg" to "에러발생!! "
                    "persist" to true
                }
            }

            Then("설정된 스케쥴링으로 이벤트 수신") {
                dispatcher.handleRequest(input)
            }
        }

        Given("S3 이벤트 수신") {
            val event = obj {
                "version" to "0"
                "id" to "195b34f3-ae17-9b41-a42b-b16fcdec8ec7"
                "detail-type" to "Object Created"
                "source" to "aws.s3"
                "account" to "992365606987"
                "time" to "2025-02-06T02:25:19Z"
                "region" to "ap-northeast-2"
                "resources" to arr[
                    "arn:aws:s3:::nabus-new-real-work"
                ]
                "detail" to obj {
                    "version" to "0"
                    "bucket" to obj {
                        "name" to "nabus-new-real-work"
                    }
                    "object" to obj {
                        "key" to "integration/ap/OUTPUT/tv-banner-07.html"
                        "size" to 1415
                        "etag" to "02e4ed8357db06eda74a7ec4cc680153"
                        "sequencer" to "0067A41D8F0A04DA0A"
                    }
                    "request-id" to "MB6EBFGP616WJ0ZQ"
                    "requester" to "992365606987"
                    "source-ip-address" to "49.254.179.205"
                    "reason" to "PutObject"
                }
            }

            Then("설정된 스케쥴링으로 이벤트 수신") {
                dispatcher.handleRequest(event)
            }
        }


    }

}
