package net.kotlinx.core.serial

import kotlinx.serialization.json.Json


/**
 * Koson json으로 변환 가능한 마커 인터페이스
 * Serializable annotation과 세트로 사용
 *  */
interface SerialJsonObj {
    fun toJson(): String
}

/** Koson json 파싱 */
interface SerialJsonCompanion {
    fun parseJson(json: String): SerialJsonObj
}

/**
 * 간단 변환도구는 만들지 않음..
 */
object SerialJsonSet {

    /** 기본 시리얼 */
    val KSON = Json { ignoreUnknownKeys = true }

    /** AWS 등 외부 시스템에 json 전달 */
    val KSON_OTHER = Json {
        this.ignoreUnknownKeys = true
        this.encodeDefaults = true //기본값을 생략하지 않게 해서 타 시스템에서 읽을 수 있게 함
    }

}