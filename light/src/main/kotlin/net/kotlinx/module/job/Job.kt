package net.kotlinx.module.job

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.AwsInfo
import net.kotlinx.aws.dynamo.*
import net.kotlinx.core.gson.GsonSet
import net.kotlinx.core.time.toIso
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * DDB에 입력되는 메타데이터
 * https://www.notion.so/mypojo/Job-Module-serverless-docker-57e773b5f0494fb59dcbff5d9a8eb8f5
 */
class Job(
    override val pk: String,
    override val sk: String,
) : DynamoData {

    override val tableName: String
        get() = TABLE_NAME

    override fun toAttribute(): Map<String, AttributeValue> {
        //인덱스 조합. 참고로 가져올때는 인덱스 없어도 됨
        return mutableMapOf<String, AttributeValue>().apply {
            this += DynamoDbBasic.PK to AttributeValue.S(pk)
            this += DynamoDbBasic.SK to AttributeValue.S(sk)

            //==================================================== 최초 생성시 필수 입력값 ======================================================
            this += Job::reqTime.name to AttributeValue.S(reqTime.toIso())
            this += Job::memberId.name to AttributeValue.S(memberId)
            this += Job::ttl.name to AttributeValue.N(ttl.toString())
            this += Job::memberReqTime.name to AttributeValue.S(memberReqTime) //인덱스용 입력
            this += Job::jobStatus.name to AttributeValue.S(jobStatus.name)
            this += Job::jobExeFrom.name to AttributeValue.S(jobExeFrom.name)
            this += Job::jobContext.name to AttributeValue.S(jobContext)
            this += Job::jobExeFromName.name to AttributeValue.S(jobExeFromName)
            //==================================================== 공통 시스템 자동(필수) 입력값 ======================================================
            startTime?.let { this += Job::startTime.name to AttributeValue.S(it.toIso()) }
            updateTime?.let { this += Job::updateTime.name to AttributeValue.S(it.toIso()) }
            endTime?.let { this += Job::endTime.name to AttributeValue.S(it.toIso()) }

            jobErrMsg?.let { this += Job::jobErrMsg.name to AttributeValue.S(it) }
            awsInfo?.let { this += Job::awsInfo.name to AttributeValue.S(GsonSet.GSON.toJson(awsInfo)) }

            jobComment?.let { this += Job::jobComment.name to AttributeValue.S(it) }
            jobOption?.let { this += Job::jobOption.name to AttributeValue.S(it) }
            sfnId?.let { this += Job::sfnId.name to AttributeValue.S(it) }
        }
    }

    override fun <T : DynamoData> fromAttributeMap(map: Map<String, AttributeValue>): T = Job(
        map[DynamoDbBasic.PK]!!.asS(), map[DynamoDbBasic.SK]!!.asS()
    ).apply {
        //==================================================== 최초 생성시 필수 입력값 ======================================================
        reqTime = map.findOrThrow(Job::reqTime)
        memberId = map.findOrThrow(Job::memberId)
        ttl = map.findOrThrow(Job::ttl)
        jobStatus = map.findOrThrow(Job::jobStatus)
        jobExeFrom = map.findOrThrow(Job::jobExeFrom)
        jobContext = map.findOrThrow(Job::jobContext)
        jobExeFromName = map.findOrThrow(Job::jobExeFromName)

        //==================================================== 공통 시스템 자동(필수) 입력값 ======================================================
        startTime = map.find(Job::startTime)
        updateTime = map.find(Job::updateTime)
        endTime = map.find(Job::endTime)

        jobErrMsg = map.find(Job::jobErrMsg)
        awsInfo = map.findJson(Job::awsInfo)
        jobComment = map.find(Job::jobComment)
        jobOption = map.find(Job::jobOption)
        sfnId = map.find(Job::sfnId)

    } as T


    //==================================================== 생성자 ======================================================
    /** 리플렉션용 기본 생성자  */
    constructor() : this("", "")

    /** sk에는 접두어를 붙여줌 */
    constructor(pk: String, sk: Long) : this(pk, "$SK_PREF$sk")

    /** 조회 조건 입력시 */
    constructor(pk: String, sk: String = "", block: Job.() -> Unit) : this(pk, sk) {
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

    /** 잡 실행 인스턴스 타입 / IP */
    lateinit var jobExeFromName: String

    /** 작업 환경 확인용 이름  ex) vcpu2,4G..  */
    lateinit var jobEnv: String

    /** JOB context (json형식). 중단 인덱스, 블락수 등의 컨텍스트 입력 . 강제 업데이트 함으로 null 아님  */
    var jobContext: String = "{}"

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

    /** AWS info. 시작되기 전까지는 null일 수 있음   */
    var awsInfo: AwsInfo? = null

    /** SFN ID  */
    var sfnId: String? = null

//    /** 실행중인 인스턴스 개별 타입  */
//    var instanceType: AwsInstanceType? = null
//
//    /**
//     * AWS의 주요 키값 정보
//     * sfn : sfnUuid
//     * lambda : awsLambdaFunctionName
//     * batch : awsBatchJobId
//     *  */
//    var instanceName: String? = null
//
//    /** 로그 그룹  */
//    var logGroupName: String? = null
//
//    /** 스트림 (API를 호출해야 알 수 있는것도 있음)  */
//    var logStreamName: String? = null

    //==================================================== 공통 옵션 입력값 ======================================================
    /** JOB 코멘트 (사용자 입력. 로직에서 사용안함. 업무 구분용)  */
    var jobComment: String? = null

    /** JOB 옵션 (json형식). 해당 잡에서 필요한 설정/옵션 값을 입력. ex) 처리 타입, 시작 날짜 등등   */
    var jobOption: String? = null

    //==================================================== 비연동값 ======================================================

    /**
     * DDB에 값을 저장할것인지?.  이 값은 DDB에 저장하지 않음.
     * JobExecuteType 에 따라 false 로 변경
     *  */
    var persist: Boolean = true

    //==================================================== 편의 메소드 ======================================================

    /** 작동시간 밀리초 리턴 */
    fun toIntervalMills(): Long? = when {
        arrayOf(startTime, endTime).any { it == null } -> null
        else -> ChronoUnit.MILLIS.between(startTime!!, endTime!!)
    }

    /**
     * 클라우드와치 로그 링크를 리턴
     * 이 뒤에 필터를 붙일 수 있다.
     */
    fun toLogLink(): String = awsInfo?.toLogLink(startTime) ?: "awsInfo is required"

    /** DDB 콘솔 링크 */
    fun toConsoleLink(): String = DynamoUtil.toConsoleLink(tableName, this)

    companion object {
        /** 테이블 이름을 여기서 지정 (한번만 지정 가능) */
        var TABLE_NAME: String = ""
            set(value) {
                if (TABLE_NAME.isNotEmpty() && TABLE_NAME != value) throw IllegalStateException("TABLE_NAME 을 두번 지정($TABLE_NAME -> ${value})하시면 안됩니다~")
                field = value
            }

        /** 아무것도 없으면 이거 입력  */
        const val DEFAULT_MEMBER = "system"

        /** SK 구조 */
        const val SK_PREF = "id#"
    }


}