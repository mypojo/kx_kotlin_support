package net.kotlinx.aws.code


//==================================================== 로직 ======================================================
///**
// * 블루그린 배포
// * https://docs.aws.amazon.com/ko_kr/codedeploy/latest/userguide/deployment-configurations.html 참고
// */
//fun CodeDeployClient.createDeployment(applicationName: String, deploymentGroupName: String, codedeployConfig: CodedeployConfig, appSpec: String): CreateDeploymentResponse {
//    val appSpecContent: AppSpecContent = AppSpecContent.builder().content(appSpec).build()
//    val revisionLocation: RevisionLocation = RevisionLocation.builder()
//        .revisionType(RevisionLocationType.APP_SPEC_CONTENT)
//        .appSpecContent(appSpecContent)
//        .build()
//    val deploymentRequest: CreateDeploymentRequest = CreateDeploymentRequest.builder()
//        .applicationName(applicationName)
//        .deploymentGroupName(deploymentGroupName)
//        .deploymentConfigName(codedeployConfig.toConfig())
//        .revision(revisionLocation)
//        .build()
//    return this.createDeployment(deploymentRequest)
//}
//
///** 후크 상태 변경  */
//fun CodeDeployClient.putLifecycleEventHookExecutionStatus(
//    deploymentId: String?,
//    lifecycleEventHookExecutionId: String?,
//    status: LifecycleEventStatus?
//): PutLifecycleEventHookExecutionStatusResponse? {
//    val req: PutLifecycleEventHookExecutionStatusRequest =
//        PutLifecycleEventHookExecutionStatusRequest.builder().deploymentId(deploymentId).lifecycleEventHookExecutionId(lifecycleEventHookExecutionId).status(status).build()
//    return this.putLifecycleEventHookExecutionStatus(req)
//}