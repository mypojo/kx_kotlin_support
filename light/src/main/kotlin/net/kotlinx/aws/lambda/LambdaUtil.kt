package net.kotlinx.aws.lambda

import net.kotlinx.aws.AwsConfig
import java.io.File
import kotlin.time.Duration.Companion.minutes

/**
 * 람다 유틸들
 *
 * 람다 파워툴스라는걸 제공하긴 한데, .. 크게 쓸모가 없어보인다
 * https://docs.powertools.aws.dev/lambda/java/
 *
 * */
object LambdaUtil {

    /** 람다 기본 타임아웃 */
    val DEAFULT_TIMEOUT = 15.minutes

    /** 람다의 로컬디렉토리 디폴트 경로. 여기부터 512mb가 할당된다.  */
    val ROOT = File("/tmp")

    //==================================================== cost ======================================================

    /** 1G 기준 초당 비용 (가장 비싸게 구매시) -> 256mb 으로 변환 */
    const val COST_GI_PER_SEC = 0.0000166667

    /** 사용량(밀리초)를 원화로 변경 */
    fun cost(mills: Long): Double = 1.0 * mills / 1000 * COST_GI_PER_SEC / 4 * AwsConfig.EXCHANGE_RATE  //256mb 기준

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

