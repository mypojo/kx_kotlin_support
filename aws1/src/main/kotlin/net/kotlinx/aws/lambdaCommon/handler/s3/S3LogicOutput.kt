package net.kotlinx.aws.lambdaCommon.handler.s3

/**
 * S3 CSV 로 저장될 개별 결과 파일.
 * input의 data 와 1:1로 매핑된다
 * */
data class S3LogicOutput(
    /** 입력값 json */
    val input: String,
    /** 결과값 json */
    val result: String,
    /** 걸린시간 */
    val durationMills: Long,
)