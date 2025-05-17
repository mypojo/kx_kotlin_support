package net.kotlinx.aws.firehose.logData

import java.time.LocalDateTime

/**
 * 통합로깅 객체
 * ex) API 서버에서 트랜잭션 로그, 민감데이터 접속로그, 로그인 로그 등등..
 * ex) 모든곳에서 특정 엔드포인트 호출 기록
 * ex) 잡 결과 기록후 리포트 작성
 * */
data class LogData(
    /** 파티션용 날짜 */
    val basicDate: String,
    /** 파티션용 구분 이름 */
    val name: String,
    /** 연결된 이벤트 ID */
    val eventId: String,
    /** 각종 추가정보 json (사용자 id 등등) */
    val metadata: String = "{}",
    //==================================================== req ======================================================
    /** 등록 시간 */
    val reqTime: LocalDateTime = LocalDateTime.now(),
    /** http uri */
    val reqUri: String,
    /** http method */
    val reqMethod: String,
    /** 헤더  */
    val reqHeader: String = "",
    /** http 바디 (페이로드)  */
    val reqBody: String = "",
    //==================================================== resp ======================================================
    /** 응답 코드  */
    val respCode: Int,
    /** 응답 바디  */
    val respBody: String,
)