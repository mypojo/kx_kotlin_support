package net.kotlinx.aws.lakeformation

import aws.sdk.kotlin.services.lakeformation.model.LfTag
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class LakeformationTagManagerTest : BeSpecHeavy() {

    private val awsClient by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("LakeformationTagManager") {

            val profile = findProfile97

            Then("일반") {
                val manager = LakeformationTagManager {
                    aws = awsClient
                    tags = listOf(
                        LfTag {
                            this.tagKey = "lake_$profile"
                            this.tagValues = listOf("common")
                        }
                    )
                    roleName = "app-admin"
                    databaseNames = listOf(
                        "d1",
                        "d2",
                        "d3",
                        "p1",
                        "p2",
                        "p3",
                    )
                }
                manager.createLfTag()
                manager.grantPermissions()
                manager.addLfTagsToResource()
            }

            Then("읽기권한") {
                val manager = LakeformationTagManager {
                    aws = awsClient
                    tags = listOf(
                        LfTag {
                            this.tagKey = "lake_$profile"
                            this.tagValues = listOf("common")
                        }
                    )
                    roleName = "app-hyper_team"
                    databaseNames = listOf(
                        "d1",
                        "d2",
                        "d3",
                        "p1",
                        "p2",
                        "p3",
                    )
                }
                manager.grantPermissionsReadonly()
            }
        }
    }

}
