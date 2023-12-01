package net.kotlinx.aws.s3

import net.kotlinx.aws.ssm.lazySsm
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class S3PropertyTest : TestRoot(){

    var demo: String by lazySsm()

    @Test
    fun test() {

    }

}