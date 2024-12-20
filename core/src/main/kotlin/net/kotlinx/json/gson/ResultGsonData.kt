package net.kotlinx.json.gson

/** 범용 결과 객체 */
data class ResultGsonData(
    val ok: Boolean,
    val data: GsonData,
)