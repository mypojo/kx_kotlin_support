package net.kotlinx.awscdk.component.sfnv2

import net.kotlinx.aws.AwsNaming
import software.amazon.awscdk.services.stepfunctions.Choice
import software.amazon.awscdk.services.stepfunctions.Condition
import software.amazon.awscdk.services.stepfunctions.IChainable

class CdkSfnChoice(
    override val cdkSfn: CdkSfn,
    override val name: String,
) : CdkSfnChain {

    override var suffix: String = ""

    override fun convert(): Choice {
        return Choice(cdkSfn.stack, "${name}${suffix}")
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