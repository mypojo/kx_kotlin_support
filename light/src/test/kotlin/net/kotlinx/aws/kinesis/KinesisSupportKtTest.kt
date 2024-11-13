package net.kotlinx.aws.kinesis

import net.kotlinx.id.Identity
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import java.util.*

class KinesisSupportKtTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("KinesisSupport") {
            Then("단건 전송") {

                data class DemoS3(
                    override val id: String,
                    val detailType: String,
                    val source: String,
                ):Identity<String>

                val demoS3 = DemoS3(UUID.randomUUID().toString(), "demo", "test")

                aws97.kinesis.putRecord("demo-s3",demoS3)
            }
        }
    }

}
