package net.kotlinx.aws.s3

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight


class S3DataTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)



        Given("S3Data") {
            val data = S3Data("myBUcket", "aa/bb/cc")

            Then("xxx") {

            }

            Then("버킷 부모 리턴") {
                data.parent.key shouldBe "aa/bb"
            }

            Then("slash 접미어 테스트1") {
                data.slash("ttp").key shouldBe "aa/bb/cc/ttp"
            }

            val data2 = S3Data("myBUcket", "aa/bb/cc/")
            Then("slash 접미어 테스트2") {
                data.key shouldNotBe data2.key  //다르지만
                data.slash("ttp").key shouldBe data2.slash("ttp").key //같다
            }

        }
    }

}
