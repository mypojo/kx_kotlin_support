package net.kotlinx.aws.lambda.snsHandler

data class LambdaSnsEvent(
    val title: String,
    val msg: String,
    /** 입력데이터 파싱 */
    val data01: String? = null,
    /** 입력데이터 파싱 */
    val data02: String? = null,
)