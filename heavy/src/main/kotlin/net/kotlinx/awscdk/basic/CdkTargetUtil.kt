package net.kotlinx.awscdk.basic

import software.amazon.awscdk.services.events.IRuleTarget
import software.amazon.awscdk.services.events.targets.LambdaFunction
import software.amazon.awscdk.services.events.targets.SnsTopic
import software.amazon.awscdk.services.lambda.IFunction
import software.amazon.awscdk.services.sns.ITopic


/** 타겟 외우기 싫어허 유틸로 뺌 */
object CdkTargetUtil {

    fun target(target: ITopic): IRuleTarget = SnsTopic(target)
    fun target(target: IFunction): IRuleTarget = LambdaFunction(target)

}