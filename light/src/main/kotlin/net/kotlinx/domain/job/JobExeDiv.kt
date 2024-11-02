package net.kotlinx.domain.job

/**
 * 잡 실행(트리거) 가능한 구분들
 * 이벤트브릿지 Rule 등록가능한 타입들
 *
 * STEP_FUNCTIONS 는 실제 코드가 작동하는 플랫폼은 아니라서 여기 해당하지 않음
 */
enum class JobExeDiv {

    /** AWS LAMBDA  */
    LAMBDA,

    /** AWS BATCH  */
    BATCH,

    /** 자기자신에서 실행. ex) 개발자 PC or WAS 등  */
    LOCAL,
}