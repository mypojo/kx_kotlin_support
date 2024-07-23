package net.kotlinx.aws.ec2

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class Ec2SupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("NAT Gateway 삭제/생성") {

            val id = "nat-04d1040e463068841"
            val elasticIpId = "eipalloc-069e36b7d72021b55"
            val subnetId = "subnet-020e2df43d9b961bf"

            Then("NAT Gateway 삭제") {
                aws.ec2.deleteNatGateway(id)
            }

            Then("NAT Gateway 생성") {
                aws.ec2.createNatGateway(subnetId, elasticIpId)
            }

        }
    }

}
