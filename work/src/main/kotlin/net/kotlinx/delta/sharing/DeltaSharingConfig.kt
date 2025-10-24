package net.kotlinx.delta.sharing

import net.kotlinx.core.Kdsl
import net.kotlinx.json.gson.GsonData

/**
 * DeltaSharingClient 설정을 위한 DSL 클래스
 */
class DeltaSharingConfig {

    @Kdsl
    constructor(block: DeltaSharingConfig.() -> Unit = {}) {
        apply(block)
    }

    /** Delta Sharing 인증 정보 (필수) */
    lateinit var secret: GsonData

    /** HTTP 요청 타임아웃 (초) */
    var timeoutInSeconds: Int = 120

    /** 재시도 횟수 */
    var numRetries: Int = 3

    /** 최대 재시도 시간 (밀리초) */
    var maxRetryDuration: Long = 600000L

    /** 재시도 간격 (밀리초) */
    var retrySleepInterval: Long = 1000L

    /** SSL 인증서 검증 무시 여부 (프로덕션에서는 false 권장) */
    var sslTrustAll: Boolean = false

    /** 스트리밍용 여부 */
    var forStreaming: Boolean = false

    /** 응답 형식: "parquet" 또는 "delta" (Deletion Vectors 사용시 "delta" 필요) */
    var responseFormat: String = "delta"

    /** Reader 기능: Deletion Vectors 기능 사용시 "deletionVectors" 설정 */
    var readerFeatures: String = "deletionVectors"

    /** 쿼리 테이블 페이지네이션 활성화 여부 */
    var queryTablePaginationEnabled: Boolean = true

    /** 요청당 최대 파일 수 */
    var maxFilesPerReq: Int = 1000

    /** 스트림 종료 액션 활성화 여부 */
    var endStreamActionEnabled: Boolean = false

    /** 비동기 쿼리 활성화 여부 */
    var enableAsyncQuery: Boolean = false

    /** 비동기 쿼리 폴링 간격 (밀리초) */
    var asyncQueryPollIntervalMillis: Long = 5000L

    /** 비동기 쿼리 최대 시간 (밀리초) */
    var asyncQueryMaxDuration: Long = 300000L

    /** 토큰 교환 최대 재시도 횟수 */
    var tokenExchangeMaxRetries: Int = 3

    /** 토큰 교환 최대 재시도 시간 (초) */
    var tokenExchangeMaxRetryDurationInSeconds: Int = 60

    /** 토큰 갱신 임계값 (초) */
    var tokenRenewalThresholdInSeconds: Int = 300
}
