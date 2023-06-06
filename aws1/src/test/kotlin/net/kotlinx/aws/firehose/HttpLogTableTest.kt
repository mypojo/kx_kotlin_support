package net.kotlinx.aws.firehose

import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test

class HttpLogTableTest : TestRoot() {

    @Test
    fun test() {
        val demo = HttpLogTable.result.apply {
            tableName = "demo"
            location = "s3://xxx/"
        }
        println(demo.drop())
        println(demo.create())
    }

}