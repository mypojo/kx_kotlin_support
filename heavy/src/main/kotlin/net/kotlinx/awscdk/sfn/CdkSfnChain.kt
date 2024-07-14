package net.kotlinx.awscdk.sfn

import software.amazon.awscdk.services.stepfunctions.State

interface CdkSfnChain {

    /** 잡 이름 */
    val cdkSfn: CdkSfn

    /** 잡 이름 */
    val name: String

    /** 중복으로 ID 충돌날경우 서픽스 */
    var suffix: String

    /** 정해진 형태를 State 로 변환 */
    fun convert(): State
}