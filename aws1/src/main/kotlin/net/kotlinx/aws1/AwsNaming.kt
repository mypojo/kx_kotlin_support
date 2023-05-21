package net.kotlinx.aws1

/**
 * AWS 공통으로 사용되는 키값 네이밍 통일
 * 범용적인 네이밍으로 사용할것
 *  */
object AwsNaming {

    //==================================================== 로직의 옵션값 관련 ======================================================

    /**
     * 각종 내부 로직의 분기 구분값
     * ex) 공용 람다 사용시 분기처리 키값
     *  */
    const val method = "method"

    /** 모드 */
    const val mode = "mode"

    /**
     * json 옵션값의 키값
     * * ex) 공용 람다 사용시 입력 옵션 json
     *  */
    const val option: String = "option"

    /**
     * 스케쥴된 시간
     * ex) SFN 예약시간
     * */
    const val scheduleTime: String = "scheduleTime"

    /**
     * 대기 시간
     * ex) SFN 대기시간
     * */
    const val waitSeconds: String = "waitSeconds"

    /** 콜드스타트 대기시간 */
    const val waitColdstartSeconds: String = "waitColdstartSeconds"

    /** 결과 등의 본문 */
    const val body: String = "body"

    //==================================================== 문자 패턴 ======================================================

    /** 콜드스타트 */
    const val choiceFirst: String = "first"

    /** 리트라이 */
    const val choiceRetry: String = "retry"


}