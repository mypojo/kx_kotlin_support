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
    val KSON = Json {
        ignoreUnknownKeys = true
    }

    /**
     * AWS 등 외부 시스템에 json 전달
     * 중요!! encodeDefaults = true 해야지 기본설정값도 전송된다. 이게 없으면 시스템에서는 역변환시 있을거라고 가정하고 무시함
     * */
    val KSON_OTHER = Json {
        this.ignoreUnknownKeys = true
        this.encodeDefaults = true
    }

}