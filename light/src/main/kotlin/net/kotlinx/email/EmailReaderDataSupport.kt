package net.kotlinx.email

import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toYmdhmKr01

/**
 * EmailReaderData 리스트 간단 출력
 * - 본문은 개행 제거 후 20자로 축약
 * - 표 형태로 정렬 출력
 */
fun List<EmailReaderData>.printSimple() {
    listOf("no", "from", "subject", "received", "body").toTextGridPrint {
        map { data ->
            arrayOf(
                data.messageNumber,
                data.from,
                data.subject,
                data.receivedDate.toYmdhmKr01(),
                data.body.toPreview(20),
            )
        }
    }
}

private fun String.toPreview(max: Int = 20): String {
    val oneLine = replace("\r", "").replace("\n", "").trim()
    return if (oneLine.length <= max) oneLine else oneLine.take(max) + "…"
}