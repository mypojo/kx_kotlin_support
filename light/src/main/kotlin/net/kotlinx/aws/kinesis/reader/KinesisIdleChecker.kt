package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.kinesis.model.Record
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import java.util.concurrent.atomic.AtomicInteger

/**
 * Kinesis 스트림의 유휴 상태(데이터가 없는 상태)를 감지하는 클래스
 * 특정 횟수만큼 연속으로 데이터가 없으면 콜백을 실행합니다.
 *
 * ex) 우선순위가 낮은 월 1회 작업은, 유휴 시간대에만 실행
 */
class KinesisIdleChecker : KinesisReaderRecordHandler, KinesisReaderEmptyHandler {

    @Kdsl
    constructor(block: KinesisIdleChecker.() -> Unit = {}) {
        apply(block)
    }

    /** 유휴 상태로 간주할 빈 응답 횟수 (기본값: 3) */
    var maxEmptyCount: Int = 3

    /** 유휴 상태 감지 시 실행할 콜백 함수 */
    lateinit var idleCallback: KinesisReaderEmptyHandler

    //==================================================== 내부사용 ======================================================

    /**
     * 빈 응답 카운트 (샤드 ID를 키로 사용)
     */
    private val emptyResponseCounts = mutableMapOf<String, AtomicInteger>()

    /** 데이터가 있으면 리셋 */
    override suspend fun invoke(shardId: String, records: List<Record>) {
        val counter = emptyResponseCounts.getOrPut(shardId) { AtomicInteger(0) }
        counter.set(0)
        log.debug { "[$shardId] 레코드 수신됨, 유휴 카운트 초기화: 0" }
    }

    /** 데이터가 없으면 카운트 증가 */
    override suspend fun invoke(shardId: String) {
        val counter = emptyResponseCounts.getOrPut(shardId) { AtomicInteger(0) }
        val count = counter.incrementAndGet()
        log.debug { "[$shardId] 빈 응답, 유휴 카운트 증가: $count" }

        // 설정된 횟수에 도달하면 콜백 실행
        if (count >= maxEmptyCount) {
            log.info { "[$shardId] 유휴 상태 감지: $count 회 연속 빈 응답" }
            idleCallback(shardId)
            // 콜백 실행 후 카운트 초기화
            counter.set(0)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}