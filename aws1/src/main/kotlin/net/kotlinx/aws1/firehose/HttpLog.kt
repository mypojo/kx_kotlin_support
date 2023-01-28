package net.kotlinx.aws1.firehose

import java.time.LocalDateTime

/** 
 * http 샘플 로깅 객체
 * */
data class HttpLog(
    /** 파티션용 날짜 */
    val basicDate: String,
    /** 파티션용 구분 이름 */
    val name: String,
    //==================================================== req ======================================================
    /** 등록 시간 */
    val reqTime: LocalDateTime = LocalDateTime.now(),
    /** http uri */
    val reqUri: String,
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