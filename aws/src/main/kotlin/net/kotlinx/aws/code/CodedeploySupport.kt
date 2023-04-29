package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codedeploy.CodeDeployClient
import aws.sdk.kotlin.services.codedeploy.createDeployment
import aws.sdk.kotlin.services.codedeploy.model.CreateDeploymentResponse
import aws.sdk.kotlin.services.codedeploy.model.LifecycleEventStatus
import aws.sdk.kotlin.services.codedeploy.model.PutLifecycleEventHookExecutionStatusResponse
import aws.sdk.kotlin.services.codedeploy.model.RevisionLocationType
import aws.sdk.kotlin.services.codedeploy.putLifecycleEventHookExecutionStatus


//==================================================== 로직 ======================================================
/**
 * 블루그린 배포
 * https://docs.aws.amazon.com/ko_kr/codedeploy/latest/userguide/deployment-configurations.html 참고
 */
suspend fun CodeDeployClient.createDeployment(
    applicationName: String,
    deploymentGroupName: String,
    appSpec: Any,
    codedeployConfig: CodedeployConfig = CodedeployConfig.ECSAllAtOnce
): CreateDeploymentResponse {
    return this.createDeployment {
        this.applicationName = applicationName
        this.deploymentGroupName = deploymentGroupName
        this.deploymentConfigName = codedeployConfig.toConfig()
        revision {
            revisionType = RevisionLocationType.AppSpecContent
            appSpecContent {
                content = appSpec.toString() //json 형식
            }
        }
    }
}

/** 후크 상태 변경 (샘플)  */
suspend fun CodeDeployClient.putLifecycleEventHookExecutionStatus(
    deploymentId: String,
    lifecycleEventHookExecutionId: String,
    status: LifecycleEventStatus
): PutLifecycleEventHookExecutionStatusResponse {
    return this.putLifecycleEventHookExecutionStatus {
        this.deploymentId = deploymentId
        this.lifecycleEventHookExecutionId = lifecycleEventHookExecutionId
        this.status = status
    }
}