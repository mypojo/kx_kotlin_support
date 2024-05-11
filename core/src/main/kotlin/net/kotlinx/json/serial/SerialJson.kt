package net.kotlinx.json.serial

/**
 * Koson json으로 변환 가능한 마커 인터페이스
 * Serializable annotation과 세트로 사용
 * class 에 붙인다.
 *  */
interface SerialToJson {
    fun toJson(): String
}

/**
 * Koson json 파싱
 * object 에 붙인다
 *  */
interface SerialParseJson {
    fun parseJson(json: String): SerialToJson
}