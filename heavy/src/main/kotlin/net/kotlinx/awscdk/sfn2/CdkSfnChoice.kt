package net.kotlinx.awscdk.sfn2

import net.kotlinx.aws.AwsNaming
import net.kotlinx.awscdk.sfn.join
import software.amazon.awscdk.services.stepfunctions.Choice
import software.amazon.awscdk.services.stepfunctions.ChoiceProps
import software.amazon.awscdk.services.stepfunctions.Condition
import software.amazon.awscdk.services.stepfunctions.IChainable

class CdkSfnChoice(
    override val sfn: CdkSfn,
    override val id: String,
    override val stateName: String,
) : CdkSfnChain {

    override fun convert(): Choice {
        return Choice(sfn.stack, "${sfn.logicalName}-${id}", ChoiceProps.builder().stateName(stateName).build())
    }
}

/**
 * 옵션 결과 body 매핑
 * @param name $.aa.bb  <-- 일반 옵션의 body를 읽는다.
 * @param pattern retry-*
 * */
fun Choice.whenMatchesBody(name: String, pattern: String, vararg states: IChainable): Choice {
    this.`when`(Condition.stringMatches("$.${AwsNaming.OPTION}.${name}.${AwsNaming.BODY}.${"state"}", pattern), states.toList().join())
    return this
}

/** 옵션에 직접 매핑 */
fun Choice.whenMatches(name: String, pattern: String, vararg states: IChainable): Choice {
    this.`when`(Condition.stringMatches("$.${AwsNaming.OPTION}.${name}", pattern), states.toList().join())
    return this
}