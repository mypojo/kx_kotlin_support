package net.kotlinx.domain.logData


import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.core.Kdsl
import net.kotlinx.json.gson.GsonData
import java.time.LocalDateTime


/**
 * 이벤트 공통 모음
 * ### 대상
 * DB 트랜잭션, 보안접근, HTTP rest 호출 등등
 *
 * ##소스들 (이벤트브릿지 소스)
 * #1. AWS 이벤트브릿지
 * #2. WEB 요청 이벤트
 * #3. JOB 처리결과 이벤트
 *  */
@Deprecated("LogData 사용하세요")
class Event {

    @Kdsl
    constructor(block: Event.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 공통 필수 ======================================================

    /**
     * 이벤트 1회당 GUI로 생성되는 유니크 ID
     * ex) 웹에서 오류일경우 이 ID를 로그에 남기고 사용자에게 리턴
     */
    var eventId: Long = -1

    /** eventTime 기준. ex) yyyymmdd.    파티셔닝에 사용됨.  */
    lateinit var eventDate: String

    /** 이벤트 시간. 문자열로 변환시 zone이 포함된 ISO 로 변환   */
    lateinit var eventTime: LocalDateTime

    /**
     * 이벤트 실행환경 ex) 람다, ECS, 등등..
     */
    lateinit var instanceType: AwsInstanceType

    /**
     * 이벤트 구분 (enum)
     * web : menuPath
     * job : job의 pk:sk
     * *   */
    lateinit var eventDiv: String

    /**
     * 이벤트 구분의 한글 설명값
     * web : menu name
     * job : job name
     * *   */
    lateinit var eventDivName: String

    /**
     * web : http 결과코드
     * job : JobStatus
     */
    lateinit var eventStatus: String

    /**
     * 이벤트 키벨류 데이터들
     * ex) campId
     */
    lateinit var body: GsonData

    /**
     * 이벤트 상세 데이터들
     * ex) DB 컬럼값 수정.
     *  */
    lateinit var datas: List<EventData>

    /**
     * 클라우드와치 로그 링크
     * awsInfoLoader 에서 가져올 수 있음
     *  */
    lateinit var logLink: String

    /** server ip  (네트워크 오류파악 등에 사용)  */
    lateinit var ip: String

    //==================================================== 공통 옵션 ======================================================

    /** 간단 파싱용 에러 메세지  */
    var errMsg: String? = null

    /** 이벤트 작동시간 (밀리초)  */
    var eventMills: Long? = null

    /**
     * 이벤트 작업 대상이 되는 회원 ID -> 복합되서 인덱싱됨
     * web : 역할 전환을 한 멤버 (관리자 id가 아님)
     * job : 단일 대상이 없으면 system 입력
     * level2 테이블은 여기로 인덱스가 걸릴 수 있음
     *  */
    var memberId: String = DEFAULT_MEMBER_ID

    //==================================================== web 필수 ======================================================

    /** http 호출한 사용자의 IP  */
    var clientIp: String? = null

    /** 역할 전환 전 사용자 (로그인한 사용자 or 관리자 ID)  */
    var loginId: String? = null

    companion object {
        const val DEFAULT_MEMBER_ID = "system"
    }


}