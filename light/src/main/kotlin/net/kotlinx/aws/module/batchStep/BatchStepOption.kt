package net.kotlinx.aws.module.batchStep

import kotlinx.serialization.Serializable
import net.kotlinx.aws.module.batchStep.stepDefault.StepStartContext

@DslMarker
annotation class BatchStepOptionDsl

/** 옵션 공통 */
@Serializable
class BatchStepOption {

    @BatchStepOptionDsl
    constructor(block: BatchStepOption.() -> Unit) {
        apply(block)
    }

    /** 잡 이름 (필수) */
    lateinit var jobPk: String

    /** 잡 SK (이것도 그냥 필수) */
    lateinit var jobSk: String

    /** 초기화로 새로 채번. 리트라이시 오버라이드. */
    lateinit var sfnId: String

    /** 이게 있으면 재시도로 간주. 재시도이면 업로드 안함.  */
    var retrySfnId: String? = null

    /** 실제 데이터를 처리할 디렉토리를 구하기 위한 ID */
    val targetSfnId: String
        get() = retrySfnId ?: sfnId

    /** LIST 모드일경우 옵션 */
    var listOption: BatchStepListOption? = null

    //==================================================== 결과 매핑 ======================================================

    val stepStart: StepStartContext? = null


}

@DslMarker
annotation class BatchStepListOptionDsl

@Serializable
class BatchStepListOption {

    @BatchStepListOptionDsl
    constructor(block: BatchStepListOption.() -> Unit) {
        apply(block)
    }

    /**
     * 최대 동시성. 최대 1000개임
     * 주의!! 상대 서버가 받아줄 수 있을 만큼만 보내기.  크롤링의 경우 너무 많으면 역효과가 남.
     * 응답속도(람다비용)이 가장 중요한 최적화 요소임.
     * 타임아웃 4초 정도 주고 간헐적으로 오류가 날 정도가 적당함
     *  */
    var maxConcurrency: Int = 100

    /**
     * 대기시간 (초). 정수로 입력해야한다.
     * 로컬에서 4초 -> 15초 정도 설정해도 랜덤하게 돌리다보면 5초 까지도 떨어짐. 2배 잡으면 될듯.
     * 주의!! 타임아웃 난다고 길게 줄 필요 없음. WAF를 회피할 정도로 입력하면 됨
     *  */
    var waitSeconds: Int = 6


}

