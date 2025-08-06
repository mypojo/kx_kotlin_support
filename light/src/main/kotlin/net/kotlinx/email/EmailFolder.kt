package net.kotlinx.email

import jakarta.mail.Folder
import jakarta.mail.Part
import jakarta.mail.internet.MimeMultipart
import jakarta.mail.internet.MimeUtility
import jakarta.mail.search.ComparisonTerm
import jakarta.mail.search.ReceivedDateTerm
import mu.KotlinLogging
import net.kotlinx.file.slash
import net.kotlinx.reflect.name
import net.kotlinx.time.toLocalDateTime
import net.kotlinx.time.toYmdhmKr01
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


/**
 * 개별로직 처리기
 * */
class EmailFolder(private val reader: EmailReader, private val folder: Folder) {

    /**
     * 현재 시간으로부터 지정된 일수(days) 동안 수신된 이메일 리스트를 가져옵니다.
     * 페이징 없이 한번에 작동함!!
     */
    fun listEmailDatas(days: Int): List<EmailReaderData> {
        log.info { "INBOX 폴더에서 최근 ${days}일간의 이메일을 조회합니다." }

        // 현재 시간으로부터 days일 전 00:00:00 이후의 메일 검색
        val startDate = Date.from(
            LocalDateTime.now().minusDays(days.toLong())
                .withHour(0).withMinute(0).withSecond(0)
                .atZone(ZoneId.systemDefault()).toInstant()
        )
        log.debug { " -> from ${startDate.toLocalDateTime().toYmdhmKr01()}" }
        val searchTerm = ReceivedDateTerm(ComparisonTerm.GE, startDate)
        val messages = folder.search(searchTerm)
        log.info { "총 ${messages.size}개의 이메일을 찾았습니다." }

        return messages.map { message ->
            EmailReaderData(
                messageNumber = message.messageNumber,
                subject = message.subject ?: "제목 없음",
                from = message.from?.firstOrNull()?.toString() ?: "발신자 미상",
                receivedDate = message.receivedDate?.toInstant()
                    ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                    ?: LocalDateTime.now(),
            )
        }
    }

    /**
     * 특정 메시지 번호의 모든 첨부파일을 다운로드합니다.
     * messageNumber로 디렉토리를 만들고 모든 첨부파일을 해당 디렉토리에 저장합니다.
     */
    fun downloadAllFiles(messageNumber: Int): List<File> {
        log.info { "메시지 번호 $messageNumber 의 모든 첨부파일을 다운로드합니다." }

        val message = folder.getMessage(messageNumber)
        val attachmentsWithParts = extractAttachmentsWithParts(message)

        if (attachmentsWithParts.isEmpty()) {
            log.warn { "메시지 번호 $messageNumber 에 첨부파일이 없습니다." }
            return emptyList()
        }

        // messageNumber로 디렉토리 생성
        val messageDir = reader.workspace.slash(this::class.name()).slash(messageNumber.toString())

        // 모든 첨부파일 다운로드
        val downloadedFiles = attachmentsWithParts.map { attachmentWithPart ->
            val fileName = attachmentWithPart.info.fileName
            log.debug { " -> 파일 다운로드 ..  $fileName" }
            val file = messageDir.slash(fileName)
            FileOutputStream(file).buffered(bufferSize = BUFFER_SIZE).use { fos ->
                attachmentWithPart.part.inputStream.use { inputStream ->
                    inputStream.copyTo(fos, bufferSize = BUFFER_SIZE)
                }
            }
            log.info { " -> 첨부파일을 다운로드 : $file" }
            file
        }

        log.info { "메시지 번호 $messageNumber 의 모든 첨부파일(${downloadedFiles.size}개)을 다운로드했습니다." }
        return downloadedFiles
    }


    /** 첨부파일 정보와 파트를 함께 담는 데이터 클래스 */
    private data class AttachmentWithPart(val info: EmailReaderFileData, val part: Part)

    /**
     * 이메일에서 첨부파일 정보와 파트를 함께 추출합니다.
     */
    private fun extractAttachmentsWithParts(part: Part): List<AttachmentWithPart> {
        return when {
            part.isMimeType("multipart/*") -> {
                val multipart = part.content as MimeMultipart
                (0 until multipart.count).flatMap { i ->
                    extractAttachmentsWithParts(multipart.getBodyPart(i))
                }
            }

            Part.ATTACHMENT.equals(part.disposition, ignoreCase = true) ||
                    Part.INLINE.equals(part.disposition, ignoreCase = true) -> {
                val fileName = part.fileName ?: "attachment_0"
                val decoded = MimeUtility.decodeText(fileName)
                val emailReaderFileData = EmailReaderFileData(
                    fileName = decoded,
                    size = part.size.toLong(),
                    contentType = part.contentType,
                    partIndex = 0
                )
                listOf(AttachmentWithPart(emailReaderFileData, part))
            }

            else -> emptyList()
        }
    }


    companion object {
        private val log = KotlinLogging.logger {}

        private const val BUFFER_SIZE = 1024 * 64
    }

}