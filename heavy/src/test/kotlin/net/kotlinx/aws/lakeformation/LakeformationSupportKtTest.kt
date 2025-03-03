package net.kotlinx.aws.lakeformation

import aws.sdk.kotlin.services.lakeformation.model.LfTag
import aws.sdk.kotlin.services.lakeformation.model.ResourceType
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class LakeformationSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("옵티마이저") {
            Then("최적화 옵션 켜기") {
                aws.lake.updateTableAllStorageOptimizer("d2", "conv_ac")
            }
        }

        Given("레이크포메이션 권한") {

            val profile = findProfile97

            val tag = LfTag {
                this.tagKey = "lake_$profile"
                this.tagValues = listOf("common")
            }

            Then("LF태그 생성") {
                val resp = aws.lake.createLfTag(tag)
                log.info { "태그결과 $resp" }
            }

            Then("데이터베이스에 태그 부착") {
                val resp = aws.lake.addLfTagsToResource("d1", listOf(tag))
                log.info { "태그결과 $resp" }
            }

            Then("역할에 태그권한 부여") {
                aws.lake.grantPermissions("app-admin", listOf(tag), ResourceType.Database)
            }
            Then("역할에 태그권한 부여 - 테이블만") {
                aws.lake.grantPermissions("app-firehose_iceberg", listOf(tag), ResourceType.Table)
            }
        }

    }

}
