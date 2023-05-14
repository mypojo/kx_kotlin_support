package net.kotlinx.aws_cdk.component.sfn

import software.amazon.awscdk.services.stepfunctions.Parallel
import software.amazon.awscdk.services.stepfunctions.ParallelProps
import software.amazon.awscdk.services.stepfunctions.State

class SfnParallel(
    /** 필드 이름 */
    override val name: String,
    private vararg val chains: Any,

    ) : SfnChain {

    override var suffix: String = ""

    override fun convert(cdk: CdkSfn): State {
        return Parallel(
            cdk.stack, "${cdk.name}-${name}", ParallelProps.builder()
                .resultPath("$.${name}-result") //resultPath 지정시 원본 +@로 리턴됨. 미지정시 해당 에리어의 모든 결과가 array로 리턴됨
                .comment("$name") //comment 가 있어야 순서도에서 예쁘게 보인다
                .build()
        ).branch(
            * chains.map { cdk.convertAny(it) }.toTypedArray() //동시에 개별 실행
        )
    }
}