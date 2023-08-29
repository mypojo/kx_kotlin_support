package net.kotlinx.module.job

/**
 * 잡 실행(트리거) 가능한 구분들
 * 이벤트브릿지 Rule 등록가능한 타입들
 */
enum class JobExeDiv {

    /** AWS LAMBDA  */
    lambda,

    /** AWS STEP FUNCTION  */
    stepFunctions,

    /** AWS BATCH  */
    batch,

    /** 자기자신에서 실행. ex) 개발자 PC or WAS 등  */
    local,
}