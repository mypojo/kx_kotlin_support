package net.kotlinx.core2.koson

import kotlinx.serialization.json.Json


/**
 * 이 이름이 맘에들어서 이렇게 붙임. koson!!
 * 간단 변환도구는 만들지 않음.. 별 필요 없어보여
 */
object KosonSet {

    /** 기본 시리얼 */
    val KSON = Json { ignoreUnknownKeys = true }

}


