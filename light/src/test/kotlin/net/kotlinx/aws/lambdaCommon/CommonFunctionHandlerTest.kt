package net.kotlinx.aws.lambdaCommon

import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test

class CommonFunctionHandlerTest : TestRoot() {

    @Test
    fun test() {

        class Sample : CommonFunctionHandler() {

            init {
                regtiter {

                }

            }

        }


    }

}