package net.kotlinx.aws.s3

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import java.io.File

class S3PropertyTest : TestRoot() {


    var workFile: File by S3Property(S3Data.parse(""))

    @Test
    fun test() {

    }

}