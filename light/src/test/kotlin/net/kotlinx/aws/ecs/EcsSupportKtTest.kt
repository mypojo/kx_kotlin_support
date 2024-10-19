package net.kotlinx.aws.ecs

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class EcsSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("ecs") {

            val profile = findProfile97
            val clusterName = "${profile}-web_cluster-dev"
            val serviceName = "${profile}-web_service-dev"

            When("ECS 서비스를 터치함") {
                Then("새로운 ECR 이미지로 배포됨") {
                    aws.ecs.touch(clusterName, serviceName)
                }
            }

            When("ECS 서비스 카운트를 수정") {
                Then("서버의 수가 조절됨") {
                    aws.ecs.updateServiceCount(clusterName, serviceName, 0)
                }
            }
        }
    }
}