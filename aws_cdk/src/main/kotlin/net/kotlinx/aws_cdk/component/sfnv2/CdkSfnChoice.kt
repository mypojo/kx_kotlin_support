package net.kotlinx.aws_cdk.component.sfnv2

import net.kotlinx.aws1.AwsNaming
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
 * 자주 사용되는 패턴. eq 도 같이 쓸수 있어서 stringMatches 를 기본으로 둔다.
 * 보통 접두어로 많이 사용됨
 * @param name $.aa.bb  <-- 일반 옵션의 body를 읽는다.
 * @param pattern retry-*
 * */
fun Choice.whenMatches(name: String, pattern: String, vararg states: IChainable): Choice {
    this.`when`(Condition.stringMatches("$.${AwsNaming.option}.${name}.${AwsNaming.body}.${"state"}", pattern), states.toList().join())
    return this
}