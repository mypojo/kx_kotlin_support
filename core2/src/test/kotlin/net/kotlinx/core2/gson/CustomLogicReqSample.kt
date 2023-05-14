package net.kotlinx.core2.gson

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * json 변환임으로 적절하게 변환. (gson 기준)
 * */
@Serializable
data class CustomLogicReqSample(
    val name: String,
    val datas: List<String>,
    private val intervalSec: Double?,
) {

    val interval: Duration? = intervalSec?.let { it.seconds }

}