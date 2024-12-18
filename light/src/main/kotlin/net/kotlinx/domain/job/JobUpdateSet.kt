package net.kotlinx.domain.job

/**
 * 단계별 업데이트 가능한 필드 지정
 * 참고로 입력은 전체임
 * */
object JobUpdateSet {

    /** 시작 */
    val START = setOf(
        net.kotlinx.domain.job.Job::jobStatus,
        net.kotlinx.domain.job.Job::jobContext,
        net.kotlinx.domain.job.Job::startTime,
        net.kotlinx.domain.job.Job::instanceMetadata,
    ).map { it.name }

    /** 중간 상태변경 */
    val STATUS = setOf(
        net.kotlinx.domain.job.Job::jobStatus,
        net.kotlinx.domain.job.Job::jobContext,
        net.kotlinx.domain.job.Job::sfnId,
    ).map { it.name }

    /** 종료 */
    val END = setOf(
        net.kotlinx.domain.job.Job::jobStatus,
        net.kotlinx.domain.job.Job::jobContext,
        net.kotlinx.domain.job.Job::endTime,
    ).map { it.name }

    /** 예외 발생시 업데이트 */
    val ERROR = (END + net.kotlinx.domain.job.Job::jobErrMsg.name)


}