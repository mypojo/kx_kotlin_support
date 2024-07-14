package net.kotlinx.awscdk.sfn

import software.amazon.awscdk.services.stepfunctions.Parallel
import software.amazon.awscdk.services.stepfunctions.ParallelProps
import software.amazon.awscdk.services.stepfunctions.State

/**
 * 동시에 개별 실행
 * */
class CdkSfnParallel(
    override val cdkSfn: CdkSfn,
    override val name: String,
    private vararg val chains: State,
) : CdkSfnChain {

    override var suffix: String = ""

    override fun convert(): State {
        return Parallel(
            cdkSfn.stack, "${cdkSfn.name}-${name}", ParallelProps.builder()
                .resultPath("$.${name}-result") //resultPath 지정시 원본 +@로 리턴됨. 미지정시 해당 에리어의 모든 결과가 array로 리턴됨
                .comment(name) //comment 가 있어야 순서도에서 예쁘게 보인다
                .build()
        ).branch(
            * chains
        )
    }
}