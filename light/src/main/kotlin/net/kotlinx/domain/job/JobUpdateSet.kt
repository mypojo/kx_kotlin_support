package net.kotlinx.domain.job

/**
 * 단계별 업데이트 가능한 필드 지정
 * 참고로 입력은 전체임
 * */
object JobUpdateSet {

    /** 시작 */
    val START = setOf(
        Job::jobStatus,
        Job::jobContext,
        Job::startTime,
        Job::instanceMetadata,
    ).map { it.name }

    /**
     * SFN 등, 상태가 연속적이여야 할 경우 중간 저장
     *  -> 이 정보는 callback 등에서 다시 로드함
     *  */
    val STATUS = setOf(
        Job::updateTime,
        Job::jobStatus,
        Job::jobContext,
    ).map { it.name }

    /**
     * SFN  정보만 미리 기입 (콜백 처리를 위함)
     *  */
    val SFN = setOf(
        Job::sfnId,
        Job::lastSfnId,
    ).map { it.name }

    /** 종료 */
    val END = setOf(
        Job::jobStatus,
        Job::jobContext,
        Job::endTime,
    ).map { it.name }

    /** 예외 발생시 업데이트 */
    val ERROR = (END + Job::jobErrMsg.name)


}