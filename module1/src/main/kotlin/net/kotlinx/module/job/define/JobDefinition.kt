package net.kotlinx.module.job.define

import net.kotlinx.module.job.run.JobRoot
import net.kotlinx.module.job.trigger.JobTrigger
import net.kotlinx.module.job.trigger.JobTriggerOption
import kotlin.reflect.KClass

/** job ENUM 에서 이걸 구현하면 됨  */
open class JobDefinition(

    /** 잡 이름. 전체 설정에서 유니크 해야함  */
    var jobPk: String = "",

    /** 이름(한글) */
    var name: String = "",

    /** 스케줄링 주가 */
    var jobScheduleType: JobScheduleType = JobScheduleType.DAY,

    /** 잡의 구현체 */
    var jobClass: KClass<out JobRoot> = JobRoot::class,

    /** 설정된 실행 구분 (설정과 다르게 실행할수도 있음!) */
    var jobTrigger: JobTrigger = JobTrigger.NON,

    /** DDB로 실행할지 여부 */
    var runAsDdb: Boolean = true,

    /** 담당자 ID ex) 문제발생시 메세지를 전달 */
    var authors: List<String> = emptyList(),

    /** 설명 */
    var comments: List<String> = emptyList(),

    /** 그룹(상위 sfn 이름 등) */
    var parentJobPk: String = "",

    ) {

    /** 한줄 코멘트 지원 */
    var comment: String = ""
        set(value) {
            comments = listOf(value)
        }

    /** 실행 옵션단계로 넘어감 */
    fun trigger(): JobTriggerOption = JobTriggerOption(this)

}

/** DSL 진입점 샘플 */
fun jobDefinitionSample(requestBlock: JobDefinition.() -> Unit): JobDefinition = JobDefinition().also { requestBlock(it) }