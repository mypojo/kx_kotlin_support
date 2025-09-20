package net.kotlinx.awscdk.sfn2

import net.kotlinx.lazyLoad.default
import software.amazon.awscdk.services.stepfunctions.Parallel
import software.amazon.awscdk.services.stepfunctions.ParallelProps
import software.amazon.awscdk.services.stepfunctions.State

/**
 * 동시에 개별 실행
 * */
class CdkSfnParallel(
    override val sfn: CdkSfn,
    override val id: String,
    override val stateName: String,
    private vararg val chains: State,
) : CdkSfnChain {

    /** 표시되는 이름 */
    var comment: String by default { id }

    /** resultPath 지정시 원본 +@로 리턴됨. 미지정시 해당 에리어의 모든 결과가 array로 리턴됨 */
    var resultPath = "$.${id}"

    override fun convert(): State {
        return Parallel(
            sfn.stack, "${sfn.logicalName}-${id}", ParallelProps.builder()
                .resultPath(resultPath)
                .comment(comment)
                .stateName(stateName)
                .build()
        ).branch(
            *chains
        )
    }
}