package net.kotlinx.aws.codeDeploy

import net.kotlinx.aws.AwsConfig

object CodedeployUtil {

    fun toConsoleLink(deploymentId: String, region: String = AwsConfig.REGION_KR): String = "https://$region.console.aws.amazon.com/codesuite/codedeploy/deployments/${deploymentId}?region=$region"

}
