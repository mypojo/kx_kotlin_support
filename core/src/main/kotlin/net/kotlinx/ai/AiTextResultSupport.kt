package net.kotlinx.ai

import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toTimeString


fun List<AiTextResult>.printSimple(rate: Double = 1480.0) {
    listOf("모델ID", "모델명", "In토큰", "Out토큰", "1M비용(원)", "걸린시간", "성공", "결과").toTextGridPrint {
        this.map {
            val won = rate * it.cost() * 1000
            arrayOf(it.model.id, it.model.name, it.inputTokens, it.outputTokens, won.toLong(), it.duration.toTimeString(), it.body.ok, it.body.data)
        }
    }
}