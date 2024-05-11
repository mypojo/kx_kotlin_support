package net.kotlinx.domain.job.trigger

import net.kotlinx.domain.job.JobExeFrom
import net.kotlinx.domain.job.define.JobDefinition

/**
 * 잡 실행(트리거) 옵션
 * 동일한 잡이라도 각각 실행 옵션이 틀리다.
 */
class JobTriggerOption(
    /** 잡 정의 */
    val jobDefinition: JobDefinition,

    ) {
    /** 잡 실행데이터는 실행시 강제 변경 가능. 디폴트로는 기본 트리거 */
    var jobTriggerMethod: JobTriggerMethod = jobDefinition.jobTriggerMethod

    /** 동기실행. (배치의 경우 컨테이너 올라갈때까지만 잠시 기다린다)  */
    var synch: Boolean = false

    //==================================================== 이하 job 입력 전달값 ======================================================

    /** 잡이 호출된 경로 */
    var jobExeFrom: JobExeFrom = JobExeFrom.ADMIN

    /** 작업 요청자 (인덱스용) */
    var memberId: String? = null

    /** SFN ID */
    var sfnId: String? = null

    /** 잡 커스텀 옵션 */
    var jobOption: Any = "{}"

    /** 실제 실행 */
    suspend fun exe(): String {
        return jobTriggerMethod.trigger(this)
    }
}