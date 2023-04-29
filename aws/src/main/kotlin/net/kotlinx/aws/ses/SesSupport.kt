package net.kotlinx.aws.ses

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.RawMessage
import aws.sdk.kotlin.services.ses.sendRawEmail
import java.io.ByteArrayOutputStream
import javax.mail.internet.MimeMessage


/**
 * 일단 이렇게만 구성한다.
 * 첨부파일때문에 일케함. 벌크 없음
 */
suspend fun SesClient.sendRawEmail(message: MimeMessage) {
    val outputStream = ByteArrayOutputStream() // use 안써도 되나??
    message.writeTo(outputStream)
    this.sendRawEmail {
        this.rawMessage = RawMessage {
            this.data = outputStream.toByteArray()
        }
    }
}
