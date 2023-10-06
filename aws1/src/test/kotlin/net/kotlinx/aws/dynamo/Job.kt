package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.core.string.toLocalDateTime
import net.kotlinx.core.time.toIso
import java.time.LocalDateTime

/**
 * DDB에 입력되는 메타데이터
 * 미리 만든 후 나중에 수정해도 되고 (일반실행  or 예약실행)
 * 실제 작동시 실행해도 됨 (상태머신 실행)
 */
open class Job(
    override val pk: String,
    override val sk: String,
) : DynamoData {

    override val tableName: String
        get() = TABLE_NAME

    override fun toAttribute(): Map<String, AttributeValue> {
        val reqTimeStr = reqTime?.toIso() ?: ""
        memberReqTime = "$memberId#$reqTimeStr"
        return mapOf(
            DynamoDbBasic.PK to AttributeValue.S(pk),
            DynamoDbBasic.SK to AttributeValue.S(sk),

            Job::ttl.name to AttributeValue.N(ttl.toString()),
            Job::memberId.name to AttributeValue.S(memberId ?: ""),
            Job::reqTime.name to AttributeValue.S(reqTimeStr),
            Job::jobStatus.name to AttributeValue.S(jobStatus ?: ""),

            //==================================================== 인덱스 입력 ======================================================
            Job::memberReqTime.name to AttributeValue.S(memberReqTime!!),
        )
    }

    /** 가져올때는 인덱스 없어도 됨 */
    override fun <T : DynamoData> fromAttributeMap(map: Map<String, AttributeValue>): T = Job(
        map[DynamoDbBasic.PK]!!.asS(),
        map[DynamoDbBasic.SK]!!.asS()
    ).apply {
        ttl = map[Job::ttl.name]?.asN()?.toLong()
        memberId = map[Job::memberId.name]?.asS()
        jobStatus = map[Job::jobStatus.name]?.asS()
        reqTime = map[Job::reqTime.name]?.asS()?.toLocalDateTime()
    } as T


    //==================================================== 생성자 ======================================================

    /** 리플렉션용 기본 생성자  */
    constructor() : this("", "")


//    //==================================================== key 분리(반정규화데이터) ======================================================
//    /** 데이터가 부분로드 될때도 있음.  */
//    fun initBean() {
//        //인덱싱 데이터 설정
//        if (memberReqTime == null) {
//            if (memberId != null && reqTime != null) {
//                memberReqTime = "$memberId#$reqTime"
//            }
//        }
//        //ttl 없으면 무조건 디폴트값이라도 입력
//        if (expireTime == null) expireTime = AwsDynamoUtil.toTtlFromNow(JobConfig.DEAULT_TTL_UNIT, JobConfig.DEAULT_TTL_VALUE)
//    }


    /** JOB 요청 시간 (복합되서 인덱싱됨)  */
    var reqTime: LocalDateTime? = null

    /** 관련 회원 ID . 없으면 system 입력. (복합되서 인덱싱됨)   */
    var memberId: String? = null

    /** TTL. null이면 자동 입력된다.  */
    var ttl: Long? = null
    //==================================================== 공통 옵션 입력값 ======================================================
    /** JOB 코멘트 (사용자 입력. 로직에서 사용안함. 업무 구분용)  */
    var jobComment: String? = null

    /** JOB 옵션 (json형식). 해당 잡에서 필요한 설정/옵션 값을 입력. ex) 처리 타입, 시작 날짜 등등   */
    var jobOption: String? = null

    /** JOB context (json형식). 중단 인덱스, 블락수 등의 컨텍스트 입력   */
    var jobContext: String? = null
    //==================================================== 분산 락 (step function) = 미구현 ======================================================
    //==================================================== getter 기타  ======================================================
    /** 이게 있으면 job 트리거에서 동시 실행 제한함. ex) KK_API  */
    var lockDiv: String? = null

    /** 이게 있으면 job 트리거에서 동시 실행 제한함. ex) 카카오 memberId  */
    var lockKey: String? = null

    /** 락 체크시마다 1회씩 증가.  */
    var lockTryCnt: Int? = null

    /** 락 체크시 타임아웃  */
    var lockTryTimeout: LocalDateTime? = null

    /** 락 대기 (분)  */
    var lockWaitMin: Int? = null
    //==================================================== 작업 예약 (step function)  ======================================================
    /**
     * job 예약날짜. 이게 있으면 스케쥴링됨
     * 예약 후 awsStepFunctionId 를 할당받아야함
     */
    var jobScheduleTime: LocalDateTime? = null


    //==================================================== csv upload 옵션 입력값 ======================================================
    /** JOB 요청 파일 경로 (s3)  ex)   /aaa/bbb/{id}  */
    var reqFilePath: String? = null

    /** JOB 요청 파일 이름 (원본이름) ex) 4월전반기실적.csv  */
    var reqFileName: String? = null
    //==================================================== 공통 시스템 자동(필수) 입력값 ======================================================
    /** 사용자가 자신이 요청한 job를 찾는용도. (인덱싱) -> {memberId}#{요청시간 밀리초}  */
    var memberReqTime: String? = null

    /** JOB 상태.   (인덱싱)  */
    var jobStatus: String? = null

    /** JOB 시작 시간  */
    var startTime: LocalDateTime? = null

    /** JOB 상태값 업데이트 시간  */
    var updateTime: LocalDateTime? = null

    /** JOB 종료 시간  */
    var endTime: LocalDateTime? = null

    /** JOB 에러메세지  */
    var jobErrMsg: String? = null

    /** 담당자 정보. DDB에는 입력하지 않음. event 로깅용  */
    var authors: List<String>? = null
    //==================================================== stream (csv 업로드) 시스템 자동(필수) 입력값 ======================================================
    /** JOB 요청 파일 크기  */
    var reqFileSize: String? = null

    /** JOB 전체 줄 수  */
    var rowTotalCnt: Long? = null

    /** JOB 성공 줄 수. atomic update  */
    var rowSuccessCnt: Long? = null

    /** JOB 실패 줄 수, atomic update  */
    var rowFailCnt: Long? = null

    /** JOB 결과 파일 경로 (s3)  */
    var resultFilePath: String? = null
    //==================================================== AWS 정보 (공통)  ======================================================
    /** 실행중인 인스턴스 개별 타입  */
    var instanceType: AwsInstanceType? = null

    /** 로그 그룹  */
    var logGroupName: String? = null

    /** 스트림 (API를 호출해야 알 수 있는것도 있음)  */
    var logStreamName: String? = null
    //==================================================== AWS 정보 (sfn)  ======================================================
    /** SFN 조회용  */
    var sfnUuid: String? = null
    //==================================================== AWS 정보 (lambda)  ======================================================
    /** AWS_LAMBDA_FUNCTION_NAME  */
    var awsLambdaFunctionName: String? = null
    //==================================================== AWS 정보 (aws-batch)  ======================================================
    /** AWS_BATCH_JOB_ID  */
    var awsBatchJobId: String? = null

    //==================================================== 편의 메소드 ======================================================


//    /** 작동시간 밀리초 리턴 */
//    fun toIntervalMills(): Long? = when {
//        arrayOf(startTime, endTime).any { it == null } -> null
//        else -> TimeUtil.interval(startTime!!, endTime!!)
//    }

//    fun toProcessRate(): BigDecimal {
//        if (!ObjectUtils.allNotNull(rowSuccessCnt, rowTotalCnt, rowFailCnt, startTime)) {
//            return BigDecimal.ZERO
//        }
//        val exeCnt = rowSuccessCnt!! + rowFailCnt!!
//        return DecimalUtil.rate(exeCnt, rowTotalCnt, 2)
//    }
//
//    fun toEstimate(): Long? {
//        if (!ObjectUtils.allNotNull(rowSuccessCnt, rowTotalCnt, rowFailCnt, startTime)) {
//            return null
//        }
//        val interval = TimeUtil.interval(startTime!!.toLocalDateTime(), LocalDateTime.now())
//        val exeCnt = rowSuccessCnt!! + rowFailCnt!!
//        val remainCnt = rowTotalCnt!! - exeCnt
//        return (1.0 * interval / exeCnt * remainCnt).toLong()
//    }

    companion object {
        /** 한번만 지정 가능 */
        var TABLE_NAME: String = ""
            set(value) {
                if (TABLE_NAME.isNotEmpty() && TABLE_NAME != value) throw IllegalStateException("TABLE_NAME 을 두번 지정($TABLE_NAME -> ${value})하시면 안됩니다~")
                field = value
            }
    }


}