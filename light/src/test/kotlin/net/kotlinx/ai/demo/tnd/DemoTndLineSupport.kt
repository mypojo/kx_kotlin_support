package net.kotlinx.ai.demo.tnd

import net.kotlinx.ai.AiTextResult
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toTimeString


fun List<AiTextResult>.toDemoTndLine(): List<DemoTndLine> = this.flatMapIndexed { resultIndex, result ->

    val titles = result.output.data["title"].map { it.str!! }
    val descs = result.output.data["desc"].map { it.str!! }

    /**
     * 퍼플렉시티의 [1][2] 이런 주석구문 제거
     * */
    val reg = """\[\d+\]""".toRegex()
    descs.mapIndexed { i, desc ->
        val title = titles.getOrElse(i) { "-" }
        val descV2 = reg.replace(desc, "")
        DemoTndLine(result, resultIndex, i, title, descV2)
    }
}

fun List<DemoTndLine>.printSimple(rate: Double = 1480.0) {
    listOf("모델명", "Out토큰", "1M비용(원)", "걸린시간", "성공", "번호", "title", "desc", "검증").toTextGridPrint {
        this.map { line ->
            val result = line.result
            val won = rate * result.cost() * 1000
            arrayOf(
                result.name, result.model.name, result.outputTokens, won.toLong(), result.duration.toTimeString(), result.output.ok,
                line.position, line.title, line.desc, line.validation
            )
        }
    }
}