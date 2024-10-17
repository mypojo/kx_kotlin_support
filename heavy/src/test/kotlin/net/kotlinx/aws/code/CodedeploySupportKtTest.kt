package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codedeploy.model.LifecycleEventStatus
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class CodedeploySupportKtTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("CodedeployAppSpecBuilder") {
            val aws by koinLazy<AwsClient>(findProfile97)
            xThen("코드 디플로이 배포") {
                val profileName = findProfile97
                val suff = "test"
                val deployData = EcsDeployData {
                    clusterName = "${profileName}-web_cluster-${suff}"
                    serviceName = "${profileName}-web_service-${suff}"
                    taskDef = "${profileName}-web_task_def-${suff}"
                    containerName = "${profileName}-web_container-${suff}"
                    beforeAllowTraffic = "${profileName}-job-${suff}"
                    containerPort = 8080
                    codedeployApplicationName = "${profileName}-web_codedeploy-${suff}"
                    codedeployDeploymentGroupName = "${profileName}-web_codedeploy_group-${suff}"
                }
                val deployment = aws.codeDeploy.createDeployment(deployData)
                log.warn { "코드디플로이 배포 -> ${CodedeployUtil.toConsoleLink(deployment.deploymentId!!)}" }
            }

            xThen("코드 디플로이 배포 -> 승인처리") {
                val deployment = aws.codeDeploy.putLifecycleEventHookExecutionStatus(
                    "d-dd",
                    "xx",
                    LifecycleEventStatus.Succeeded,
                )
                log.warn { "코드디플로이 승인됨 from ${deployment.lifecycleEventHookExecutionId}" }
            }
        }
    }
}