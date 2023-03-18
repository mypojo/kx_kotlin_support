package net.kotlinx.aws.code

import net.kotlinx.aws1.AwsConfig

object CodedeployUtil {

    fun toConsoleLink(deploymentId: String, region: String = AwsConfig.SEOUL): String = "https://$region.console.aws.amazon.com/codesuite/codedeploy/deployments/${deploymentId}?region=$region"

}
