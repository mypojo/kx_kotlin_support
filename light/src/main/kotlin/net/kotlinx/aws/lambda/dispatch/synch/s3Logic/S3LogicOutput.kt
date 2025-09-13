package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

import mu.KotlinLogging
import net.kotlinx.json.gson.GsonData
import net.kotlinx.string.print

/**
 * S3 CSV 로 저장될 개별 결과 파일.
 * input의 data 와 1:1로 매핑된다
 * */
data class S3LogicOutput(
    /** 입력값 json -> 결과 json에는 이게 입력됨 */
    val input: GsonData,
    /** 결과값 json */
    val result: GsonData,
    /** 결과를 S3로 업로드 할지 여부 */
    val write: Boolean = true,
) {

    /** 테스트 등의 간단 출력용 */
    fun printResultSimple(cnt: Int = 6) {
        result.entryMap().forEach { e ->
            log.warn { "=== ${e.key} (결과 ${e.value.size}건 요약..) ===" }
            e.value.take(cnt).print()
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}

        /** 빈값 리턴 */
        fun empty(): S3LogicOutput = S3LogicOutput(GsonData.empty(), GsonData.empty(), false)

    }

}