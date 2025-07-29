package net.kotlinx.aws.kinesis.writer

import aws.sdk.kotlin.services.kinesis.model.PutRecordsRequestEntry
import aws.sdk.kotlin.services.kinesis.putRecords
import com.google.gson.Gson
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.aws.kinesis.kinesis
import net.kotlinx.core.Kdsl
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.GsonSet
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * KPL (Kinesis Producer Library) 를 사용하면 더 최적화 가능하지만, java 버전이라 안씀
 * 그냥도 쓸만함
 */
class KinesisWriter {

    @Kdsl
    constructor(block: KinesisWriter.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정 ======================================================

    /** aws 클라이언트 */
    var aws: AwsClient by LazyAwsClientProperty()

    /** 스트림 이름 */
    lateinit var streamName: String

    /**
     * 파티션 키 빌더
     * 고정값을 리턴하는경우
     * 1. 모든값은 고정된 샤드로 전송된다  = 로드밸런싱 되지 않음
     * 2. 하지만 동일 샤드에서는 순서가 유지됨
     * 대량의 데이터 입력시 "키워드" 처럼 분산된 값을 사용해야함
     *  */
    lateinit var partitionKeyBuilder: (GsonData) -> String

    /** JSON 직렬화에 사용할 Gson 인스턴스 (기본값: GsonSet.TABLE_UTC_WITH_ZONE) */
    var gson: Gson = GsonSet.TABLE_UTC_WITH_ZONE

    /** 최대 재시도 횟수 */
    var maxRetries: Int = 10

    /** 재시도 간 지연 시간 (기본값: 1초) */
    var retryDelay: Duration = 1.seconds

    //==================================================== 기능 ======================================================

    /**
     * 다수의 객체(data class)를 Kinesis 스트림에 입력
     * 실패한 레코드에 대해 자동으로 재시도 처리
     * 1. 전체 레코드를 Kinesis에 전송
     * 2. 응답에서 실패한 레코드만 필터링
     * 3. 실패한 레코드만 재시도
     * 4. 모든 레코드가 성공하거나 최대 재시도 횟수에 도달할 때까지 반복
     * @param inputs 입력할 데이터 객체 목록
     * @return 최종 PutRecordsResponse 객체
     */
    suspend fun putRecords(inputs: List<GsonData>, fixedPartitionKey: String? = null) {
        // 초기 데이터 매핑 - 요청 엔트리 생성
        val entries = inputs.map { input ->
            PutRecordsRequestEntry {
                partitionKey = fixedPartitionKey ?: partitionKeyBuilder(input)
                data = gson.toJson(input.delegate)!!.toByteArray()
            }
        }

        // 처리할 레코드 목록 (초기값은 모든 레코드)
        var remainingRecords = entries
        // 현재 시도 횟수
        var retryCount = 0

        // 모든 레코드가 처리될 때까지 반복
        while (remainingRecords.isNotEmpty() && retryCount <= maxRetries) {
            // putRecords 로 전송해야함
            val response = aws.kinesis.putRecords {
                this.streamName = this@KinesisWriter.streamName
                this.records = remainingRecords
            }

            // 실패한 레코드가 없으면 종료
            if (response.failedRecordCount == 0) {
                break
            }

            // 실패한 레코드만 필터링하여 다음 시도를 위해 준비
            remainingRecords = response.records.mapIndexedNotNull { index, result ->
                if (result.errorCode != null) {
                    //로그 너무 많으면 내리기
                    log.warn { " => failed to put record to Kinesis. index=$index, partitionKey=${entries[index].partitionKey}, errorCode=${result.errorCode}, errorMessage=${result.errorMessage}" }
                    // 실패한 레코드는 유지
                    remainingRecords[index]
                } else {
                    // 성공한 레코드는 제외
                    null
                }
            }

            // 실패한 레코드가 있고 최대 재시도 횟수에 도달하지 않았으면 재시도
            if (remainingRecords.isNotEmpty() && retryCount < maxRetries) {
                retryCount++
                // 로깅
                log.warn { " => retry [KinesisPutRecords] $retryCount/$maxRetries => Failed to put ${remainingRecords.size} records to Kinesis. Retrying..." }
                // 지연
                delay(retryDelay)
            } else if (remainingRecords.isNotEmpty()) {
                // 최대 재시도 횟수에 도달했지만 여전히 실패한 레코드가 있는 경우
                log.warn { "Failed to put ${remainingRecords.size} records to Kinesis after $maxRetries retries." }
                throw IllegalStateException("Failed to put ${remainingRecords.size} records to Kinesis after $maxRetries retries.")
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}