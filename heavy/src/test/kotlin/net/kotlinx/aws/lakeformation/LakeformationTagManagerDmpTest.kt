package net.kotlinx.aws.lakeformation

import aws.sdk.kotlin.services.lakeformation.model.LfTag
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class LakeformationTagManagerDmpTest : BeSpecHeavy() {

    private val awsClient by lazy { koin<AwsClient>(findProfile49) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("LakeformationTagManager") {

            val profile = findProfile49

            val tagAdmin = LfTag {
                this.tagKey = "admin"
                this.tagValues = listOf("dev")
            }

            Then("리소스에 태그 부착 (일단 데이터베이스 전체를 통으로 줌)") {
                val manager = LakeformationTagManager {
                    aws = awsClient
                    tags = listOf(tagAdmin)
                    databaseNames = listOf("ds", "ps")
                }
                manager.addLfTagsToResource()
            }

        }
    }

}
