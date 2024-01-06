package net.kotlinx.slack.template

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class SlackSimpleAlertTest : TestRoot(){


    @Test
    fun test() {

        val alert = SlackSimpleAlert {
            source = "demo"
            workDiv = "test"
            mainMsg = "??"
        }
        alert.send()

    }

}
