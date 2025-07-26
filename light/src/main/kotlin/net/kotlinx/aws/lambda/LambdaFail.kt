package net.kotlinx.aws.lambda

import net.kotlinx.json.gson.GsonSet
import net.kotlinx.string.ResultText

/**
 * 람다 호출 실패시 json 컨버팅 가능한 객체
 * 어디쓰지??
 * */
@Deprecated("어디쓰는지 모르겠음")
data class LambdaFail(
    val errorType: String,
    val errorMessage: String,
    val stackTrace: List<String>,
    val cause: LambdaFail?,
) {

    companion object {
        fun from(resultText: ResultText) = GsonSet.GSON.fromJson(resultText.result, LambdaFail::class.java)!!
    }

}