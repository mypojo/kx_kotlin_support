package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockagent.BedrockAgentClient
import aws.sdk.kotlin.services.bedrockagent.getIngestionJob
import aws.sdk.kotlin.services.bedrockagent.model.IngestionJobStatus
import aws.sdk.kotlin.services.bedrockagent.startIngestionJob
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.kotlinx.core.VibeCoding


private val log = KotlinLogging.logger {}

/**
 * S3 변경 목록을 로깅하고, Knowledge Base 재처리를 트리거
 * Knowledge Base는 S3의 업데이트 시간 기준으로 수정된거만 동기화함. 필요시 동기화 옵션 사용할것!
 */
@VibeCoding
suspend fun BedrockAgentClient.syncKnowledgeBase(
    knowledgeBaseId: String,
    dataSourceId: String,
    waitUntilComplete: Boolean = true,
): IngestionJobStatus? {

    val startRes = this.startIngestionJob {
        this.knowledgeBaseId = knowledgeBaseId
        this.dataSourceId = dataSourceId
    }
    val jobId = startRes.ingestionJob!!.ingestionJobId
    log.info { "KnowledgeBase($knowledgeBaseId) jobId($jobId) 작업시작..." }

    if (!waitUntilComplete) return null

    return this.waitIngestionJobCompletion(
        knowledgeBaseId = knowledgeBaseId,
        dataSourceId = dataSourceId,
        ingestionJobId = jobId,
    )
}


/**
 * Ingestion Job 완료까지 대기. 실패/중단 시 예외 발생
 */
@VibeCoding
suspend fun BedrockAgentClient.waitIngestionJobCompletion(
    knowledgeBaseId: String,
    dataSourceId: String,
    ingestionJobId: String,
    pollIntervalMillis: Long = 3_000,
    timeoutMillis: Long = 10 * 60_000,
): IngestionJobStatus {
    val startAt = System.currentTimeMillis()
    while (true) {
        when (val status = getIngestionJobStatus(knowledgeBaseId, dataSourceId, ingestionJobId)) {
            IngestionJobStatus.Complete -> {
                log.info { "KnowledgeBase($knowledgeBaseId) IngestionJob($ingestionJobId) 완료" }
                return status
            }

            IngestionJobStatus.Failed, IngestionJobStatus.Stopped -> {
                throw IllegalStateException("KnowledgeBase($knowledgeBaseId) IngestionJob($ingestionJobId) 실패 상태: $status")
            }

            else -> {
                if (System.currentTimeMillis() - startAt > timeoutMillis) {
                    throw IllegalStateException("KnowledgeBase($knowledgeBaseId) IngestionJob($ingestionJobId) 타임아웃")
                }
                delay(pollIntervalMillis)
            }
        }
    }
}

/** 현재 Ingestion Job 상태 조회 */
suspend fun BedrockAgentClient.getIngestionJobStatus(
    knowledgeBaseId: String,
    dataSourceId: String,
    ingestionJobId: String,
): IngestionJobStatus? = getIngestionJob {
    this.knowledgeBaseId = knowledgeBaseId
    this.dataSourceId = dataSourceId
    this.ingestionJobId = ingestionJobId
}.ingestionJob?.status