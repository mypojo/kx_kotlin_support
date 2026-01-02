package net.kotlinx.aws.ses

import aws.sdk.kotlin.services.ses.model.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class SesSupportKtTest : BeSpecHeavy() {

    private val profileName by lazy { findProfile49 }
    private val aws by lazy { koin<AwsClient>(profileName) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("ses") {

            Then("이메일 전송") {

                log.warn { "이메일이 실제 발송됩니다.!!" }

                val emailBody = Message {
                    this.subject = Content {
                        data = "테스트이메일 v3"
                    }
                    body = Body {
                        html = Content {
                            data = createHTML().html {
                                body {
                                    h1 {
                                        +"안녕하세요"
                                    }
                                    p {
                                        +"여기는 테스트입니다."
                                    }
                                }
                            }
                        }
                    }
                }

                val emailRequest = SendEmailRequest {
                    destination = Destination {
                        toAddresses = listOf(
                            "seunghan.shin@nhnad.com",
                        )
                    }
                    message = emailBody
                    source = "seunghan.shin@nhnad.com"
                }

                aws.ses.sendEmail(emailRequest)
            }
        }
    }

}