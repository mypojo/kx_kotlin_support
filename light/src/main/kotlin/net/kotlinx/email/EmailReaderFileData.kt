package net.kotlinx.email

/**
 * 이메일 첨부파일 정보를 담는 데이터 클래스
 */
data class EmailReaderFileData(
    val fileName: String,
    val size: Long,
    val contentType: String,
    val partIndex: Int
)