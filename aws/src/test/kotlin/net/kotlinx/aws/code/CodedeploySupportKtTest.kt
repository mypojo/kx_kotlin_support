package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codedeploy.model.LifecycleEventStatus
import aws.sdk.kotlin.services.codedeploy.putLifecycleEventHookExecutionStatus
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.core2.test.TestRoot
import org.junit.jupiter.api.Test

class CodedeploySupportKtTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin", awsId = "112233").toAwsClient()

    @Test
    fun test() {

        val appSepc = CodedeployAppSpecBuilder(
            awsId = aws.awsConfig.awsId!!,
            containerName = "sin-web_container-prod",
            taskDef = "sin-web_task_def-prod",
            lambdaHookName = "sin-controller-prod"
        ).build()

        runBlocking {
            val deployment = aws.codeDeploy.createDeployment("sin-codedeploy-prod", "sin-codedeploy-prod", appSepc)
            println(CodedeployUtil.toConsoleLink(deployment.deploymentId!!))
        }
    }

    @Test
    fun to_success() = runBlocking {
        val deployment = aws.codeDeploy.putLifecycleEventHookExecutionStatus {
            "d-9ZEA0GBXJ"
            "eyJlbmNyeXB0ZWREYXRhIjoiNmNWNjd1N0Fsbm1TWmo3L1lJMmZtWmlqdC9nMDdMdC8yYTlCQUh2QXV1RUtDUkZ1ZXBmMWNRWTdsRCs2blN5RXpWenNQWXhWT0g2SEVNbTFpKzlMKzRTMFhjWk0yUHRTU0FxRXhjekFiUGFPS0JJRjR2bG9pR1hWakpXUmthOXVTSVRZdVJDdmlRPT0iLCJpdlBhcmFtZXRlclNwZWMiOiJQYnRSNzg2Zm1BSE02ODVxIiwibWF0ZXJpYWxTZXRTZXJpYWwiOjF9"
            LifecycleEventStatus.Succeeded
        }
        println(deployment)
    }

}