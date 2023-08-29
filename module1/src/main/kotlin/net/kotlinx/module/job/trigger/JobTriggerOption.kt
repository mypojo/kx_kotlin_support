package net.kotlinx.module.job.trigger

import net.kotlinx.module.job.JobExeFrom
import net.kotlinx.module.job.define.JobDefinition

/**
 * 잡 실행(트리거) 옵션
 * 동일한 잡이라도 각각 실행 옵션이 틀리다.
 */
data class JobTriggerOption(
    /** 잡 정의 */
    val jobDefinition: JobDefinition,

    ) {
    /** 잡 실행데이터는 실행시 강제 변경 가능 */
    var currentJobExeData: JobTrigger = jobDefinition.jobTrigger

    /** 동기실행. (배치의 경우 컨테이너 올라갈때까지만 잠시 기다린다)  */
    var synch: Boolean = false

    /** 어떤 방법으로 실행하려고 하는지? */
    var jobExeFrom: JobExeFrom = JobExeFrom.ADMIN

    /** 잡 옵션 (DDB에 직접 입력할때만 적용됨) */
    var jobOption: Any = "{}"

    /** 최종 실행 */
    suspend fun fire() {
        currentJobExeData.trigger(this)
    }
}