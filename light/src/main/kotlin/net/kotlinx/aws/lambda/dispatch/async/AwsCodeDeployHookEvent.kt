package net.kotlinx.aws.lambda.dispatch.async

import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent

data class AwsCodeDeployHookEvent(val lifecycleEventHookExecutionId: String, val deploymentId: String) : AwsLambdaEvent