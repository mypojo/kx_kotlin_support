package net.kotlinx.aws.lakeformation

import aws.sdk.kotlin.services.lakeformation.model.LfTag
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class LakeformationTagManagerDmp : BeSpecHeavy() {

    private val awsClient by lazy { koin<AwsClient>(findProfile49) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("LakeformationTagManager") {

            val profile = findProfile49

            val tagAdmin = LfTag {
                this.tagKey = "admin"
                this.tagValues = listOf("all")
            }

            Then("태그 생성") {
                val manager = LakeformationTagManager {
                    aws = awsClient
                    tags = listOf(tagAdmin)
                }
                manager.createLfTag()
            }

            Then("리소스에 태그 부착 (일단 데이터베이스 전체를 통으로 줌)") {
                val manager = LakeformationTagManager {
                    aws = awsClient
                    tags = listOf(tagAdmin)
                    databaseNames = listOf("ds", "ps")
                }
                manager.addLfTagsToResource()
            }

            Then("특정 권한에 태그 부여 (관리자권한)") {
                val manager = LakeformationTagManager {
                    aws = awsClient
                    tags = listOf(tagAdmin)
                    roleName = "app-firehose_iceberg"
                }
                manager.grantPermissions()
            }

            Then("특정 권한에 태그 부여 (읽기전용)") {
                val teamName = "hyper"
                val manager = LakeformationTagManager {
                    aws = awsClient
                    tags = listOf(
                        LfTag {
                            this.tagKey = "team_${teamName}"
                            this.tagValues = listOf("all")
                        }
                    )
                    roleName = "app-team_${teamName}"
                }
                manager.grantPermissionsReadonly()
            }
        }
    }

}
