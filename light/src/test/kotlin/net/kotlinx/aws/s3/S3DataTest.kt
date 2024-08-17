package net.kotlinx.aws.s3

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class S3DataTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)


        Given("S3Data") {
            val data = S3Data("myBUcket", "aa/bb/cc")
            Then("버킷 부모 리턴") {
                data.parent.key shouldBe "aa/bb"
            }
        }
    }

}
