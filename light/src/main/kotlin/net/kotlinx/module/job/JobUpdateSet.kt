package net.kotlinx.module.job

/**
 * 단계별 업데이트 가능한 필드 지정
 * 참고로 입력은 전체임
 * */
object JobUpdateSet {

    /** 시작 */
    val START = listOf(
        Job::jobStatus,
        Job::jobContext,
        Job::startTime,
        Job::awsInfo,
    ).map { it.name }

    /** 종료 */
    val END = listOf(
        Job::jobStatus,
        Job::jobContext,
        Job::endTime,
    ).map { it.name }

    /** 예외 발생시 업데이트 */
    val ERROR = (END + Job::jobErrMsg.name)


}