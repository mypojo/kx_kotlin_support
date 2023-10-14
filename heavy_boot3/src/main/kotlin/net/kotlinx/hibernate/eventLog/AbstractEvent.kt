package net.kotlinx.hibernate.eventLog


import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.core.gson.GsonData
import net.kotlinx.hibernate.eventLog.data.EventData
import java.time.LocalDateTime


/** 이벤트 공통 모음 */
open class AbstractEvent {

    //==================================================== 필수 ======================================================

    /**
     * 유니크 ID
     * GUI로 채번. 오류일경우 이 ID를 로그에 남기고 사용자에게 리턴.
     */
    var eventId: Long = 0

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
     * job : jobDiv
     * *   */
    lateinit var eventDiv: String

    /** 이벤트 명 (한글or영문)  */
    lateinit var eventName: String

    /**
     * web : http 결과코드
     * job : JObStatus
     */
    lateinit var eventStatus: String

    /** 이벤트 상세 데이터들  */
    lateinit var datas: List<EventData>

    /**
     * 이벤트 키벨류 데이터들
     * ex) campId
     */
    lateinit var data: GsonData

    /**
     * 클라우드와치 로그 링크
     * awsInfoLoader 에서 가져올 수 있음
     *  */
    lateinit var logLink: String

    /** server ip  (네트워크 오류파악 등에 사용)  */
    lateinit var ip: String

    //==================================================== 옵션 ======================================================


    /** 간단 파싱용 에러 메세지  */
    var errMsg: String? = null

    /**
     * 최대 100분할된 해시값.  파티셔닝에 사용됨.
     * ex) 사용자 ID를 90으로 해시분할
     * *   */
    var eventHash: String? = null

    /** 담당 개발자 ID  */
    var author: String? = null

    /** 이벤트 작동시간 (밀리초)  */
    var eventMills: Long? = null

}