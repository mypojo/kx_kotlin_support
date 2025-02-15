package net.kotlinx.ai.demo.tnd

import net.kotlinx.ai.AiTextInput
import net.kotlinx.ai.AiTextResult
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.number.padStart

data class DemoTndLine(
    val result: AiTextResult,
    val resultIndex: Int,
    val index: Int,
    val title: String,
    val desc: String,
) {

    val position: String
        get() = "${(resultIndex + 1).padStart(4)}-${(index + 1).padStart(2)}"

    val validation: String
        get() = "title(${title.length}) / desc(${desc.length})"

    /** 결과 csv 라인 */
    val liens: List<String>
        get() {
            val inp = result.input[0] as AiTextInput.AiTextString
            val inputGson = inp.text.toGsonData()
            return listOf(
                inputGson[0].str!!, inputGson[1].str!!, inputGson[2].str!!,
                position,
                title, desc, validation,
            )
        }

}