package net.kotlinx.domain.job

import net.kotlinx.aws.AwsInstanceMetadata
import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.aws.dynamo.enhanced.DbItem
import net.kotlinx.aws.fargate.FargateUtil
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.reflect.name
import net.kotlinx.time.toIso
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * DDB에 입력되는 메타데이터
 * https://www.notion.so/mypojo/Job-Module-serverless-docker-57e773b5f0494fb59dcbff5d9a8eb8f5
 */
class Job(override val pk: String, override val sk: String) : DbItem {

    //==================================================== 생성자 ======================================================
    /** 리플렉션용 기본 생성자. 일반 호출은 금지  */
    @Deprecated("향후 삭제")
    constructor() : this("", "")

    /**
     * 조회 조건 입력시
     * null이 안됨으로 sk는 공백으로 체크해야함
     *  */
    constructor(pk: String, sk: String = "", block: Job.() -> Unit = {}) : this(pk, sk) {
        block(this)
    }

    //==================================================== 최초 생성시 필수 입력값 ======================================================

    /** JOB 요청 시간 (복합되서 인덱싱됨)  */
    lateinit var reqTime: LocalDateTime

    /** 관련 회원 ID . 없으면 system 입력. (복합되서 인덱싱됨)   */
    var memberId: String = DEFAULT_MEMBER

    /** TTL. */
    var ttl: Long = 0L

    /** JOB 상태.   (인덱싱)  */
    lateinit var jobStatus: JobStatus

    /** 잡 실행 경로. (RMI 실행, 스케쥴링 실행..) */
    lateinit var jobExeFrom: JobExeFrom

    /** 작업 환경 확인용 이름  ex) vcpu2,4G..  */
    var jobEnv: String? = null

    /** JOB context (json형식). 중단 인덱스, 블락수 등의 컨텍스트 입력 . 강제 업데이트 함으로 null 아님  */
    var jobContext: GsonData = GsonData.empty()

    //==================================================== 시스템 자동 입력값 ======================================================
    /** 사용자가 자신이 요청한 job를 찾는용도. (인덱싱) -> {memberId}#{요청시간 밀리초}  */
    val memberReqTime: String
        get() = "$memberId#${reqTime.toIso()}"

    //==================================================== 공통 시스템 자동(필수) 입력값 ======================================================
    /** JOB 시작 시간  */
    var startTime: LocalDateTime? = null

    /** JOB 상태값 업데이트 시간  */
    var updateTime: LocalDateTime? = null

    /** JOB 종료 시간  */
    var endTime: LocalDateTime? = null

    /** JOB 에러메세지  */
    var jobErrMsg: String? = null

    //==================================================== AWS info ======================================================

    /**
     * AWS info.
     * 혹시 런타임이 틀릴 수 있어서 실제 잡 시작할때 입력함
     *  시작되기 전까지는 null일 수 있음.
     *  */
    var instanceMetadata: AwsInstanceMetadata? = null

    /**
     * SFN ID
     * 일반 잡이 아니라 SFN 스텝 전체를 1개의 잡으로 입력
     * */
    var sfnId: String? = null

    //==================================================== 공통 옵션 입력값 ======================================================

    /** JOB 옵션 (json형식). 해당 잡에서 필요한 설정/옵션 값을 입력. ex) 처리 타입, 시작 날짜 등등   */
    var jobOption: GsonData = GsonData.empty()

    /**
     * 잡 옵션을 클래스로 매핑한경우 , 객체로 변경해서 리턴해줌.
     * 없을경우 null을 리턴
     *  */
    inline fun <reified T> jobOptionClass(): T? = this.jobOption[T::class.name()].str?.toGsonData()?.fromJson<T>()

    //==================================================== 비연동값 ======================================================

    /**
     * DDB에 값을 저장할것인지?.  이 값이 false 이면 jobRepository에서 DDB에 저장하지 않음.
     * JobExecuteType 에 따라 false 로 변경
     *  */
    var persist: Boolean = true

    //==================================================== 편의 메소드 ======================================================

    /** 작동시간 밀리초 리턴 */
    val intervalMills: Long?
        get() = when {
            arrayOf(startTime, endTime).any { it == null } -> null
            else -> ChronoUnit.MILLIS.between(startTime!!, endTime!!)
        }


    /**
     * 매우 러프하게 원화 단위로 비용을 계산해준다
     * 이 코드를 참조해서 실제 계산할것
     *  */
    val cost: Double?
        get() {
            val sumOfInterval = intervalMills ?: return null
            return when (instanceMetadata?.instanceType) {
                AwsInstanceType.LAMBDA -> LambdaUtil.cost(sumOfInterval)
                AwsInstanceType.BATCH -> FargateUtil.cost(0.5, 1.0, sumOfInterval)
                else -> null
            }
        }

    /**
     * 클라우드와치 로그 링크를 리턴
     * 이 뒤에 필터를 붙일 수 있다.
     */
    val cloudWatchLogLink: String?
        get() = instanceMetadata?.toLogLink(startTime)

    companion object {

        /** 아무것도 없으면 이거 입력  */
        const val DEFAULT_MEMBER = "system"

    }


}