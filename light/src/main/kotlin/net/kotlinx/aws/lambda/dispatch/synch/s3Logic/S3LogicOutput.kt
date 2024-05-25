package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

import net.kotlinx.json.gson.GsonData

/**
 * S3 CSV 로 저장될 개별 결과 파일.
 * input의 data 와 1:1로 매핑된다
 * */
data class S3LogicOutput(
    /** 입력값 json */
    val input: GsonData,
    /** 결과값 json */
    val result: GsonData,
)