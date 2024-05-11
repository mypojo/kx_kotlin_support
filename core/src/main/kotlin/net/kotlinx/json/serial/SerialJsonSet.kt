package net.kotlinx.json.serial

import kotlinx.serialization.json.Json


/**
 * 간단 변환도구는 만들지 않음..
 */
object SerialJsonSet {

    /**
     * 일반적으로 변경될거라 생각되는 기본 시리얼
     *  */
    val JSON = Json {
        ignoreUnknownKeys = true
    }

    /**
     * AWS 등 외부 시스템에 json 전달
     * 중요!! encodeDefaults = true 해야지 기본설정값도 전송된다. 이게 없으면 시스템에서는 역변환시 있을거라고 가정하고 무시함
     * */
    val JSON_OTHER = Json {
        this.ignoreUnknownKeys = true
        this.encodeDefaults = true
    }

}