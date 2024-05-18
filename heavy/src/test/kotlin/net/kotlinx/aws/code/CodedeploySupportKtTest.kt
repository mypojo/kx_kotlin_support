package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codedeploy.model.LifecycleEventStatus
import aws.sdk.kotlin.services.codedeploy.putLifecycleEventHookExecutionStatus
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class CodedeploySupportKtTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("CodedeployAppSpecBuilder") {
            val aws by koinLazy<AwsClient>()
            xThen("코드 디플로이 배포") {

                val deployData = EcsDeployData {
                    containerName = "sin-web_container-prod"
                    taskDef = "sin-web_task_def-prod"
                    beforeAllowTraffic = "sin-controller-prod"
                    applicationName = "sin-codedeploy-prod"
                    deploymentGroupName = "sin-codedeploy-prod"
                }
                val deployment = aws.codeDeploy.createDeployment(deployData)
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