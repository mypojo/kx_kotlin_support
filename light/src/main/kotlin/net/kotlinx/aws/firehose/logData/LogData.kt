package net.kotlinx.aws.firehose.logData

import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.json.gson.GsonData
import java.time.LocalDateTime

/**
 * 통합로깅 객체
 * ex) API 서버나 배치 작업에서의 트랜잭션단위 로그
 * ex) API 서버의 민감데이터 접속로그, 로그인 로그 등
 * ex) 타사 API 호출로그.  메타 API 실행이력 등..
 * ex) 잡 결과 기록후 리포트 작성
 *
 * 동일한 eventId 로 여러 로우가 입력될 수 있음
 * */
data class LogData(

    //==================================================== 파티션 정보 ======================================================

    /** 파티션용 날짜 */
    val basicDate: String,

    /**
     * 파티션용  구분 이름
     * ex) 프로젝트A, 프로젝트B
     *  */
    val projectName: String,

    //==================================================== 그룹 데이터 ======================================================

    /**
     * 이벤트 구분
     * ex) API  => API 서버 요청/응답
     * ex) JOB => 실행/종료 (람다 포함)
     * */
    val eventDiv: String,

    /**
     * 이벤트 1회당 GUI로 생성되는 유니크 ID
     * ex) JOB => JOB ID => 잡 1회 실행
     * ex) API => GUID 채번 => 버튼 1회 클릭
     * ex) 웹에서 오류일경우 이 ID를 로그에 남기고 사용자에게 리턴
     *
     * 주의!! 편의상 1개 요청은 1개의 TX를 가진다고 가정
     *  */
    val eventId: String,

    /**
     * 이벤트 시간 = 트랜잭션 구분 시간
     * 트랜잭션 시간의 정의는 업무별로 다름으로 주의!
     * 별도의 종료시간은 기록하지 않음 (이벤트 작동시간으로 대체)
     *  */
    val eventTime: LocalDateTime = LocalDateTime.now(),

    /**
     * 이벤트 실행환경 ex) 람다, ECS, 등등..
     */
    val instanceType: AwsInstanceType,

    //==================================================== 트랜잭션 공통 ======================================================

    /**
     * 이벤트 구분
     * web : menu path
     * job : job pk
     * *   */
    val eventName: String,

    /**
     * 이벤트 구분의 한글 설명값 (쿼리로 내용파악 가능하게)
     * web : menu A => menu B => menu C 등등..
     * job : job 한글명
     * *   */
    val eventDesc: String,

    /**
     * 이벤트들의 최종 결과
     * web : http 결과코드
     * job : JobStatus
     */
    val eventStatus: String,

    /**
     * 이벤트 전체 작동시간 (밀리초)
     *  */
    val eventMills: Long,

    /**
     * 각종 추가정보
     * clientIp : http 호출한 사용자의 IP
     * loginId : 역할 전환 전 사용자 (로그인한 실제 사용자 or 관리자 ID)
     * */
    val metadata: GsonData,

    //==================================================== 상세 데이터들 ======================================================

    /**
     * 이벤트 작업 대상이 되는 회원 ID -> 주로 필터링 조건으로 사용됨
     * web : 역할 전환을 한 멤버 (관리자 id가 아님)
     * job : 단일 대상이 없으면 system 입력
     *  */
    val memberId: String,

    /**
     * group01 의 줄임말 (json 용량때문)
     * 이벤트의 대분류
     * ex) jpa
     * ex) service
     * ex) media_meta,pg_toss
     */
    val g1: String,

    /**
     * 이벤트의 중분류
     * ex) jpa => ${DB테이블명},
     * ex) service => login, audit, rolwSwitch ..
     * ex) pg_toss => https://aa/bb/cc
     */
    val g2: String,

    /**
     * 이벤트의 세분류
     * ex) jpa => ${I,U,D}
     * ex) pg_toss => POST
     */
    val g3: String,

    /**
     * 조회 가능한 키워드
     * jpa => DB의 PK (GUID) => 해당 테이블의 변경 로그를 조회 가능
     * audit => 개인정보 식별자 => 특정 개인정보에 접근한 사용자 조회가능
     */
    val keyword: String,

    /**
     * 이벤트의 입력 본문
     * ex) jpa => 더티체크 정보
     * ex) API 요청시 쿼리스트링, 헤더정보, 바디정보 등등
     */
    val x: GsonData,

    /**
     * 이벤트의 출력 본문
     * ex) 메타 API 호출시 응답바디,
     * ex) 에러 메세지, JPA 변경데이터 등등
     * ex) 사용자 AA가 중요데이터 BB에 접근 등등
     * ex) jpa : 변경후 엔티티
     */
    val y: GsonData,

)