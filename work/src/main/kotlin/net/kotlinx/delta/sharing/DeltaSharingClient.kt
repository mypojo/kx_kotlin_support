package net.kotlinx.delta.sharing

import io.delta.sharing.client.BearerTokenDeltaSharingProfile
import io.delta.sharing.client.DeltaSharingProfileProvider
import io.delta.sharing.client.DeltaSharingRestClient
import io.delta.sharing.client.model.DeltaTableFiles
import io.delta.sharing.client.model.Table
import scala.Option
import scala.collection.immutable.Seq

/**
 * DeltaSharingRestClient를 래핑하는 클라이언트
 * DSL 스타일로 설정을 구성하여 사용
 *
 * 사용 예시:
 * ```kotlin
 * val client = DeltaSharingClient(DeltaSharingConfig().apply {
 *     secret = file.readText().toGsonData()
 *     timeoutInSeconds = 120
 *     responseFormat = "delta"
 * })
 * ```
 */
class DeltaSharingClient(config: DeltaSharingConfig) {

    val restClient: DeltaSharingRestClient

    init {
        // secret에서 인증 프로필 생성
        val profile = BearerTokenDeltaSharingProfile(
            Option.apply(config.secret["shareCredentialsVersion"].int),
            config.secret["endpoint"].str,
            config.secret["bearerToken"].str,
            config.secret["expirationTime"].str
        )

        // DeltaSharingProfileProvider 구현
        val provider = DeltaSharingProfileProvider { profile }

        // DeltaSharingRestClient 생성
        restClient = DeltaSharingRestClient(
            provider,
            config.timeoutInSeconds,
            config.numRetries,
            config.maxRetryDuration,
            config.retrySleepInterval,
            config.sslTrustAll,
            config.forStreaming,
            config.responseFormat,
            config.readerFeatures,
            config.queryTablePaginationEnabled,
            config.maxFilesPerReq,
            config.endStreamActionEnabled,
            config.enableAsyncQuery,
            config.asyncQueryPollIntervalMillis,
            config.asyncQueryMaxDuration,
            config.tokenExchangeMaxRetries,
            config.tokenExchangeMaxRetryDurationInSeconds,
            config.tokenRenewalThresholdInSeconds
        )
    }

    /**
     * 모든 테이블 목록 조회
     */
    fun listAllTables(): List<Table> = restClient.listAllTables()?.toKoltinList()!!

    /**
     * 테이블 버전 조회
     */
    fun getTableVersion(table: Table, startingTimestamp: Option<String> = Option.empty()) = restClient.getTableVersion(table, startingTimestamp)

    /**
     * 테이블 파일 목록 조회 (최신 스냅샷)
     *
     * client.getFiles(table, 0L, Option.empty()) 이렇게 하면 전체 히스토리를 가져옴
     */
    fun getFiles(
        table: Table,
        predicateHints: Seq<String> = emptySet<String>().toScalaSeq()!!,
        limitHint: Option<Any> = Option.empty(),
        version: Option<Any> = Option.empty(),
        timestamp: Option<String> = Option.empty(),
        jsonPredicateHints: Option<String> = Option.empty(),
        refreshToken: Option<String> = Option.empty()
    ): DeltaTableFiles = restClient.getFiles(table, predicateHints, limitHint, version, timestamp, jsonPredicateHints, refreshToken)
}
