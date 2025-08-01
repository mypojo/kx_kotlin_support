package net.kotlinx.email

import java.time.LocalDateTime

/**
 * 이메일 기본 정보를 담는 데이터 클래스
 */
data class EmailReaderData(
    val messageNumber: Int,
    val subject: String,
    val from: String,
    val receivedDate: LocalDateTime,
)

