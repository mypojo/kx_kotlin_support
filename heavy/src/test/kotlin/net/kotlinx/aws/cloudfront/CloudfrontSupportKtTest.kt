package net.kotlinx.aws.cloudfront

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class CloudfrontSupportKtTest : BeSpecHeavy() {

    private val client by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("CloudfrontSupport") {
            Then("단순 조회") {
                val distribution = client.cloudFront.getDistribution("E1IB941WRI0DZN")
                println("Distribution Details:")
                println("Domain Name: ${distribution.domainName}")
                println("Distribution ID: ${distribution.id}")
                println("ARN: ${distribution.arn}")
                println("Status: ${distribution.status}")
                println("Enabled: ${distribution.distributionConfig?.enabled}")
                println("Origins:")
                distribution.distributionConfig?.origins?.items?.forEach { origin ->
                    println("\t- Domain Name: ${origin.domainName}")
                    println("\t- Origin ID: ${origin.id}")
                    println("\t- Protocol Policy: ${origin.customOriginConfig?.originProtocolPolicy}")
                }
            }

            Then("오리진 변경") {

                val blueGreenDeployment = CloudfrontBlueGreenDeployment {
                    client = this@CloudfrontSupportKtTest.client
                    distributionId = "E1IB941WRI0DZN"
                    blue = "demo" to "xx-static-dev.s3.ap-northeast-2.amazonaws.com"
                    green = "green" to "xx-static-prod.s3.ap-northeast-2.amazonaws.com"
                    invalidationPaths = listOf(
                        "/static/*",
                    )
                }
                blueGreenDeployment.switch()


            }
        }


    }

}
