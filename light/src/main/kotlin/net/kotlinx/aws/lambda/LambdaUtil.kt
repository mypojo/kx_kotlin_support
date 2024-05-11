package net.kotlinx.aws.lambda

import net.kotlinx.json.gson.GsonSet
import net.kotlinx.string.ResultText
import java.io.File

/** 람다 유틸들  */
object LambdaUtil {

    /** 람다의 로컬디렉토리 디폴트 경로. 여기부터 512mb가 할당된다.  */
    val ROOT = File("/tmp")

    //==================================================== cost ======================================================

    /** 1G 기준 초당 비용 (가장 비싸게 구매시) -> 256mb 으로 변환 */
    const val COST_GI_PER_SEC = 0.0000166667

    //==================================================== 간단 예약어들  ======================================================

    /** 공용 람다 내부에서 각 실행들의 구분 키값 */
    const val METHOD = "method"

    //==================================================== 결과 간단  ======================================================

    /** 정상 결과 리턴 문자열 */
    const val OK = "ok"

    /** 정상 결과 리턴 문자열 */
    const val FAIL = "fail"

    /** 스냅스타트용 예약어 */
    const val BEFORE_CHECKPOINT = "beforeCheckpoint"

    /** 실 서비스를 나타내는 람다 Alias 예약어 */
    const val SERVICE_ON = "serviceOn"

    /** URL 직접호출시 쿼리스트링을 추출한다. */
    @Suppress("UNCHECKED_CAST")
    fun queryString(event: Map<String, Any>): Map<String, *> = event["queryStringParameters"] as Map<String, *>

}

/** 람다 호출 실패시 json 컨버팅 가능한 객체 */
data class LambdaFail(
    val errorType: String,
    val errorMessage: String,
    val stackTrace: List<String>,
    val cause: LambdaFail?
) {

    companion object {
        fun from(resultText: ResultText) = GsonSet.GSON.fromJson(resultText.result, LambdaFail::class.java)!!
    }

}

//val lambdaFail = GsonSet.GSON.fromJson(resp.result, LambdaFail::class.java)
//log.warn { "실패(람다호출) : ${lambdaFail.errorType} => ${lambdaFail.errorMessage}" }
//if (log.isDebugEnabled) {
//    lambdaFail.stackTrace.forEach { log.debug { " -> $it" } }
//}