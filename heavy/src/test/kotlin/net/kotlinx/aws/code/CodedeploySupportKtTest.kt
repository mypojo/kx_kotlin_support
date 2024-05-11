package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codedeploy.model.LifecycleEventStatus
import aws.sdk.kotlin.services.codedeploy.putLifecycleEventHookExecutionStatus
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.sts.StsUtil
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class CodedeploySupportKtTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("CodedeployAppSpecBuilder") {
            val aws = koin<AwsClient>()
            xThen("코드 디플로이 배포") {
                val appSepc = CodedeployAppSpecBuilder(
                    awsId = StsUtil.ACCOUNT_ID,
                    containerName = "sin-web_container-prod",
                    taskDef = "sin-web_task_def-prod",
                    lambdaHookName = "sin-controller-prod"
                ).build()

                val deployment = aws.codeDeploy.createDeployment("sin-codedeploy-prod", "sin-codedeploy-prod", appSepc)
                log.warn { "코드디플로이 배포 -> ${CodedeployUtil.toConsoleLink(deployment.deploymentId!!)}" }
            }

            xThen("코드 디플로이 배포 -> 승인처리") {
                val deployment = aws.codeDeploy.putLifecycleEventHookExecutionStatus {
                    "xxxx"
                    "yyyy"
                    LifecycleEventStatus.Succeeded
                }
                log.warn { "코드디플로이 승인됨 from ${deployment.lifecycleEventHookExecutionId}" }
            }
        }
    }
}