package net.kotlinx.aws.lakeformation

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class LakeformationSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.FAST)

        Given("레이크포메이션") {

            val profile = findProfile97

            val tag = "lake_$profile" to listOf("common")

            Then("LF태그 생성") {
                aws.lake.createLfTag(tag)
            }

            Then("데이터베이스에 태그 부착") {
                aws.lake.addLfTagsToResource("d1", listOf(tag))
            }
        }
    }

}
