package net.kotlinx.awscdk.sfn2

import net.kotlinx.aws.AwsNaming
import software.amazon.awscdk.services.stepfunctions.State

interface CdkSfnChain {

    /** 부모 */
    val sfn: CdkSfn

    /** ID */
    val id: String

    /** 상태 이름 (한글가능) */
    val stateName: String

    /** 정해진 형태를 State 로 변환 */
    fun convert(): State

    /**
     * 결과 패스 참조
     * A -> B 로 실행될때 A의 결과를 B가 사용하기 위해서 참조함
     * */
    fun resultPath(path: String): String = "$.${AwsNaming.OPTION}.${id}.${AwsNaming.BODY}.${path}"
}