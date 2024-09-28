package net.kotlinx.aws.lambda.dispatch

import com.lectra.koson.obj
import net.kotlinx.aws.lambda.dispatch.asynch.SnsEventPublisher
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class LambdaDispatcherSnsTest : BeSpecHeavy() {

    private val dispatcher by koinLazy<LambdaDispatcher>()

    init {
        initTest(KotestUtil.IGNORE)

        Given("SNS 테스트") {

            Then("CodePipeline") {
                val input = obj {
                    SnsEventPublisher.EVENT_SOURCE to SnsEventPublisher.SOURCE_SNS
                    "Sns" to obj {
                        "Type" to "Notification"
                        "Message" to obj {
                            "detailType" to "CodePipeline Pipeline Execution State Change"
                            "detail" to obj {
                                "pipeline" to "test-pipeline"
                                "state" to "STARTED"
                            }
                        }.toString()
                    }
                }
                log.debug { "테스트 파라메터 $input" }
                dispatcher.handleRequest(input)
            }

            Then("클라우드와치 알람") {
                val input = obj {
                    SnsEventPublisher.EVENT_SOURCE to SnsEventPublisher.SOURCE_SNS
                    "Sns" to obj {
                        "Message" to obj {
                            "AlarmName" to "테스트알람"
                            "Trigger" to obj {
                                "aa" to "xx"
                                "bb" to "yy"
                            }
                        }.toString()
                    }
                }
                log.debug { "테스트 파라메터 $input" }
                dispatcher.handleRequest(input)
            }

        }

    }

}
