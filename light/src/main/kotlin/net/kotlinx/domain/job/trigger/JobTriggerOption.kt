package net.kotlinx.domain.job.trigger

import net.kotlinx.core.Kdsl
import net.kotlinx.domain.job.JobExeFrom
import net.kotlinx.domain.job.define.JobDefinition

/**
 * 잡 실행(트리거) 옵션
 * 동일한 잡이라도 각각 실행 옵션이 틀리다.
 */
class JobTriggerOption {

    @Kdsl
    constructor(block: JobTriggerOption.() -> Unit = {}) {
        apply(block)
    }

    /** 필수입력항목임!! */
    lateinit var jobPk: String

    /**
     * 잡 실행데이터는 실행시 최초 설정과는 다르게 강제 변경 가능.
     * */
    var jobTriggerMethod: JobTriggerMethod = JobTriggerMethod.LOCAL

    /** 동기실행. (배치의 경우 컨테이너 올라갈때까지만 잠시 기다린다)  */
    var synch: Boolean = false

    //==================================================== 이하 job 입력 전달값 ======================================================

    /** 여기 입력되면 이거 사용. 없으면 새로 채번함 */
    var jobSk: String? = null

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

    @Kdsl
    constructor(jobDefinition: JobDefinition) {
        jobPk = jobDefinition.jobPk
        jobTriggerMethod = jobDefinition.jobTriggerMethod
    }
}