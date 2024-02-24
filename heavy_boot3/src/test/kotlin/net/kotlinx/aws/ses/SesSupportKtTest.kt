package net.kotlinx.aws.ses

import aws.sdk.kotlin.services.ses.model.*
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsClient
import net.kotlinx.test.MyHeavyKoinStarter
import net.kotlinx.test.TestHeavy
import org.junit.jupiter.api.Test
import org.koin.core.component.inject

class SesSupportKtTest : TestHeavy() {

    companion object {
        init {
            MyHeavyKoinStarter.startup("meta")
        }
    }

    val aws: AwsClient by inject()

    @Test
    fun test() {

        val subject = "테스트이메일 v3"

        // The HTML body of the email.
        val bodyHTML = (
                "<html>" + "<head></head>" + "<body>" + "<h1>Hello!</h1>" +
                        "<p> See the list of customers.</p>" + "</body>" + "</html>"
                )


        val contentOb = Content {
            data = bodyHTML
        }

        val bodyOb = Body {
            html = contentOb
        }

        val msgOb = Message {
            this.subject = Content {
                data = subject
            }
            body = bodyOb
        }

        val emailRequest = SendEmailRequest {
            destination = Destination {
                toAddresses = listOf(
                    "mypojo@11h11m.com",
                    "my.pojo@gmail.com",
                )
            }
            message = msgOb
            source = "meta@11h11m.com"
        }


        runBlocking {
            aws.ses.sendEmail(emailRequest)
        }

    }


}