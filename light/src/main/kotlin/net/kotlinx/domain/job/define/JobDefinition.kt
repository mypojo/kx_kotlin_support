package net.kotlinx.domain.job.define

import net.kotlinx.core.Kdsl
import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.domain.job.JobTasklet
import net.kotlinx.domain.job.trigger.JobTriggerMethod
import net.kotlinx.domain.job.trigger.JobTriggerOption
import net.kotlinx.reflect.name
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours


/** 잡 정의 등록 (초기화시 모두 생성해서 등록함) */
fun Module.jobReg(block: JobDefinition.() -> Unit) {
    val defForPk = JobDefinition(block) //pk 확인을 위해서 그냥 생성한다.
    single(named(defForPk.jobPk)) { JobDefinition(block) }  //named 지정하면서 생성은 안되네.. ㅠㅠ
}

/** job ENUM 에서 이걸 구현하면 됨  */
class JobDefinition {

    @Kdsl
    constructor(block: JobDefinition.() -> Unit = {}) {
        apply(block)
    }

    /** 잡의 구현체 등록. 이 설정은 말 그대로 설정이라서 실제 인스턴스화까지는 되지 말아야함  */
    lateinit var jobClass: KClass<out JobTasklet>

    /** 잡 이름. 전체 설정에서 유니크 해야함. 보통 jobClass 의 decapital()  */
    val jobPk: String
        get() = jobClass.name()

    /** 이름(한글) */
    lateinit var name: String

    /** 스케줄링 주가. */
    var jobScheduleType: JobScheduleType = JobScheduleType.NONE

    /** 설정된 기본 잡 실행 설정 (설정과 다르게 실행할수도 있음!) */
    var jobTriggerMethod: JobTriggerMethod = JobTriggerMethod.LOCAL

    /** 잡 실행타입. */
    var jobExecuteType: JobExecuteType = JobExecuteType.NORMAL

    /** 담당자 ID ex) 문제발생시 메세지를 전달 */
    var authors: List<DeveloperData> = emptyList()

    /** 설명 */
    var descs: List<String> = emptyList()

    /**
     * 속성
     * 커스텀 해서 사용하세요
     * */
    var attributes: MutableMap<String, Any> = mutableMapOf()

    /** 그룹(상위 sfn 이름 등) */
    var parentJobPk: String = ""

    /** 기본적인 잡의 타임아웃. 이게 넘어가면 경고 등이 실행되어야함 */
    var timeout: Duration = 1.hours

    /** 설정에서 커스텀 옵션으로 변경함 */
    fun toJobOption(block: JobTriggerOption.() -> Unit = {}): JobTriggerOption = JobTriggerOption(this).apply(block)

}

