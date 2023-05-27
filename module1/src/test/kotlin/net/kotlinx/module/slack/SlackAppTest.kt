package net.kotlinx.module.slack

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test

class SlackAppTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

    @Test
    fun test() {

        val token = aws.ssmStore["/slack/nov/token"] !!

        val app = SlackApp(token)
        app.chatPostMessage("U037LL28D4P","aaa")




    }

}