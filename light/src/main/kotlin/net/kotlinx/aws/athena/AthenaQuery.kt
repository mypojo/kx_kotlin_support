package net.kotlinx.aws.athena

import aws.sdk.kotlin.services.athena.model.QueryExecution
import java.io.File

sealed interface AthenaQuery {
    /** 쿼리 */
    val query: String
}

data class AthenaExecute(
    /** 쿼리 */
    override val query: String,
    /** 기본 콜백 */
    val callback: (suspend (QueryExecution) -> Unit)? = null,
) : AthenaQuery

data class AthenaReadAll(
    /** 쿼리 */
    override val query: String,
    /** CSV line 인메모리 읽기 */
    var callback: suspend (List<List<String>>) -> Unit = {}
) : AthenaQuery {
    /** 결과 */
    var lines: List<List<String>>? = null
}

data class AthenaDownload(
    /** 쿼리 */
    override val query: String,
    /** 파일 다운로드 (스트림 처리용) */
    var callback: (File) -> Unit
) : AthenaQuery {
    /** 결과 */
    var file: File? = null
}