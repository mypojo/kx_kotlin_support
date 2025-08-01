package net.kotlinx.aws.athena

import aws.sdk.kotlin.services.athena.model.QueryExecution
import net.kotlinx.aws.s3.S3Data
import java.io.File
import java.util.*

/**
 * 파일 분리하고싶은데.. 이미 많이 써서 일단 둔다
 * */
sealed interface AthenaQuery {
    /** 쿼리 */
    val query: String

    /**
     * 멱등 검증용 클라이언트 토큰
     * InvalidRequestException -> Idempotent parameters do not match 때문에 임시조치. 향후 기능 추가하자
     * */
    var token: String?
}

data class AthenaExecute(
    /** 쿼리 */
    override val query: String,
    /** 기본 콜백 */
    val callback: (suspend (QueryExecution) -> Unit)? = null,
) : AthenaQuery {
    override var token: String? = UUID.randomUUID().toString()

    /** S3 결과 저장 */
    lateinit var outputLocation: String

    /** 결과 S3 경로 */
    val queryResultPath: S3Data
        get() = S3Data.parse(outputLocation)
}

data class AthenaReadAll(
    /** 쿼리 */
    override val query: String,
    /** CSV line 인메모리 읽기 */
    var callback: suspend (List<List<String>>) -> Unit = {},
) : AthenaQuery {

    override var token: String? = UUID.randomUUID().toString()

    /** 결과 */
    var lines: List<List<String>>? = null
}

data class AthenaDownload(
    /** 쿼리 */
    override val query: String,
    /** 파일 다운로드 (스트림 처리용) */
    var callback: (File) -> Unit = {},
) : AthenaQuery {
    /** 결과 */
    var file: File? = null

    override var token: String? = UUID.randomUUID().toString()
}