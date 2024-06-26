package net.kotlinx.json.serial

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy


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

    /**
     * JSON_OTHER 의 언더스코어 버전
     * kakao가 이런 식으로 전달해줌
     * */
    @OptIn(ExperimentalSerializationApi::class)
    val JSON_OTHERU = Json {
        this.ignoreUnknownKeys = true
        this.encodeDefaults = true
        this.namingStrategy = JsonNamingStrategy.SnakeCase
    }

}