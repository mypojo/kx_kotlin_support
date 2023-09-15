package net.kotlinx.core.koson

import kotlinx.serialization.json.Json


/**
 * Koson json으로 변환
 * Serializable annotation과 세트로 사용
 *  */
interface KosonObj {
    fun toJson(): String
}

/** Koson json 파싱 */
interface KosonCompanion {
    fun parseJson(json: String): KosonObj
}

/**
 * 이 이름이 맘에들어서 이렇게 붙임. koson!!
 * 간단 변환도구는 만들지 않음.. 별 필요 없어보여
 */
object KosonSet {

    /** 기본 시리얼 */
    val KSON = Json { ignoreUnknownKeys = true }


    /** AWS 등 외부 시스템에 json 전달 */
    val KSON_OTHER = Json {
        this.ignoreUnknownKeys = true
        this.encodeDefaults = true //기본값을 생략하지 않게 해서 타 시스템에서 읽을 수 있게 함
    }

}


