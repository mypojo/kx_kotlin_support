package net.kotlinx.aws.firehose

import java.time.LocalDateTime

/**
 * http 샘플 로깅 객체
 * 파티션 설계에 따라 매우 달라질 수 있음
 * 이 로그는 너무 많기때문에 이벤트 로그와 같이 보여주는것은 힘들듯함
 * 보통 eventId 혹은 respBody 를 파싱해서 조회
 * */
data class HttpLog(
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