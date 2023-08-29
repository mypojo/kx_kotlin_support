package net.kotlinx.module.job

/**
 * 잡 상태에 없는게 있어서 새로 만들었음
 * @see software.amazon.awssdk.services.batch.model.JobStatus
 *
 * 중간에 상태 생략 가능
 * SCHEDULED -> STARTING -> RUNNING -> WAITING -> SUCCEEDED or FAILED or CANCELED
 *
 */
enum class JobStatus(val desc: String) {

    //==================================================== 시작전 최초 상태 ======================================================
    /**
     * 작업이 제출되어 스케쥴링 된 상태 (상태머신 전용)
     * 상태머신의 wait = 고정시간(Timestamp) 사용됨
     * ex) 특정 작업이 다음주 월요일 오후 X시에 실행되길 원함
     */
    SCHEDULED("예약됨"),

    /**
     * 실행을 요청했고, 아직 실행중이지 않은 상태.
     * ex) 스케줄러 or WAS에서 AWS JOB or lambda 를 제출했으나 아직 소스코드 실행 전 (도커 콜드스타트중)
     */
    STARTING("시작중"),  //==================================================== 작동중 ======================================================

    /** 실행중  */
    RUNNING("실행중"),

    /**
     * 다른 IO를 기다리는 상황 (상태머신 전용)
     * 상태머신의 wait = 간격초(seconds) 사용됨
     * ex) 리포트 요청 API 날린 후 대기.
     * ex) athena 쿼리 요청 후 결과를 기다림
     */
    WAITING("대기중"),  //==================================================== 종료 ======================================================

    /** 정상 처리 성공  */
    SUCCEEDED("성공"),

    /** 에러 등으로 인한 실패  */
    FAILED("실패"),

    /**
     * 사용자의 (예약중 상태에서) 취소
     * SCHEDULED -> CANCELED
     */
    CANCELED("취소");

    /** 종료된 상태인지?  */
    fun finished(): Boolean = this in arrayOf(SUCCEEDED, FAILED, CANCELED)

    /** 실행 가능한 상태인지?  */
    fun readyToRun(): Boolean = this in arrayOf(STARTING, SCHEDULED)

}