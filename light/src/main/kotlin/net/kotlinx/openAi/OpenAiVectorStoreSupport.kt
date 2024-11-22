package net.kotlinx.openAi

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.vectorstore.VectorStore
import net.kotlinx.number.toLocalDateTime
import net.kotlinx.number.toSiText
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toKr01


/**
 * 어시스턴트  이름이나 스래드 등이 표시되지 않음
 * */
@OptIn(BetaOpenAI::class)
fun List<VectorStore>.printSimple() {
    listOf("id", "name", "createdAt", "status", "용량","파일수","expire", "lastActiveAt").toTextGridPrint {
        this.sortedByDescending { it.createdAt }.map {
            arrayOf(
                it.id.id,
                it.name,
                it.createdAt.toLocalDateTime().toKr01(),
                it.status.value,
                it.usageBytes.toSiText(),
                it.fileCounts.total,
                it.expiresAt?.toLocalDateTime()?.toKr01() ?: "-",
                it.lastActiveAt?.toLocalDateTime()?.toKr01() ?: "-"
            )
        }
    }
}