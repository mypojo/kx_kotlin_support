package net.kotlinx.domain.eventLog


open class EventJob : AbstractEvent() {

    //==================================================== 필수 ======================================================

    /** 잡 실행 구분 (트리거에서 넣어줌)  */
    lateinit var jobExeDiv: String

    /** 관련 회원 ID . 없으면 system 입력. (복합되서 인덱싱됨)   */
    lateinit var memberId: String

    //==================================================== 옵션 ======================================================

    /** JOB 전체 줄 수  */
    var rowTotalCnt: Long? = null

    /** JOB 성공 줄 수. atomic update  */
    var rowSuccessCnt: Long? = null

    /** JOB 실패 줄 수, atomic update  */
    var rowFailCnt: Long? = null
}