package net.kotlinx.aws.ses

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.RawMessage
import aws.sdk.kotlin.services.ses.sendRawEmail
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


/**
 * 일단 이렇게만 구성한다.
 * 첨부파일때문에 일케함. 벌크 없음
 */
suspend fun SesClient.sendRawEmail(message: MimeMessage) {
    val outputStream = ByteArrayOutputStream() // use 안써도 되나??
    withContext(Dispatchers.IO) {
        message.writeTo(outputStream)
    }
    this.sendRawEmail {
        this.rawMessage = RawMessage {
            this.data = outputStream.toByteArray()
        }
    }
}
