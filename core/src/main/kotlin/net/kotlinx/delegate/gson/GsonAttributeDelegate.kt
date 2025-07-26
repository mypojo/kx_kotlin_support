package net.kotlinx.delegate.gson

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import mu.KotlinLogging
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.GsonSet
import kotlin.reflect.KProperty

/**
 * Gson 기반의 속성 위임자
 * JSON 형식의 속성을 위임 패턴으로 쉽게 접근하고 수정할 수 있게 해줌
 */
class GsonAttributeDelegate<T> {

    /**
     * 속성 값을 가져오는 연산자 함수
     * 속성 이름을 키로 사용하여 GsonData에서 값을 가져옴
     */
    operator fun getValue(thisRef: GsonAttribute, property: KProperty<*>): T {
        val gsonData = thisRef.attributes.get(property.name)
        
        if (gsonData.isNull) {
            log.warn { "속성 맵에 ${property.name}가 존재하지 않습니다" }
            throw IllegalStateException("속성 맵에 ${property.name}가 존재하지 않습니다")
        }
        
        @Suppress("UNCHECKED_CAST")
        return when {
            // String 타입 체크
            Int::class.java.isInstance(0) && gsonData.int != null -> gsonData.int as T
            Long::class.java.isInstance(0L) && gsonData.long != null -> gsonData.long as T
            Float::class.java.isInstance(0f) && gsonData.delegate.isJsonPrimitive && gsonData.delegate.asJsonPrimitive.isNumber -> 
                gsonData.delegate.asJsonPrimitive.asFloat as T
            Double::class.java.isInstance(0.0) && gsonData.delegate.isJsonPrimitive && gsonData.delegate.asJsonPrimitive.isNumber -> 
                gsonData.delegate.asJsonPrimitive.asDouble as T
            Boolean::class.java.isInstance(false) && gsonData.bool != null -> gsonData.bool as T
            String::class.java.isInstance("") && gsonData.str != null -> gsonData.str as T
            JsonElement::class.java.isInstance(JsonNull.INSTANCE) -> gsonData.delegate as T
            GsonData::class.java.isInstance(gsonData) -> gsonData as T
            else -> gsonData.delegate as T
        }
    }

    /**
     * 속성 값을 설정하는 연산자 함수
     * 속성 이름을 키로 사용하여 GsonData에 값을 설정
     */
    operator fun setValue(thisRef: GsonAttribute, property: KProperty<*>, value: T) {
        when (value) {
            is String -> thisRef.attributes.put(property.name, value)
            is Number -> thisRef.attributes.put(property.name, value)
            is Boolean -> thisRef.attributes.put(property.name, value)
            is JsonElement -> thisRef.attributes.put(property.name, GsonData(value))
            is GsonData -> thisRef.attributes.put(property.name, value)
            null -> thisRef.attributes.put(property.name, null as String?)
            else -> {
                // Gson을 사용하여 객체를 JsonElement로 변환
                val gson = GsonSet.GSON
                val jsonElement = gson.toJsonTree(value)
                thisRef.attributes.put(property.name, GsonData(jsonElement))
            }
        }
    }
    
    companion object {
        private val log = KotlinLogging.logger {}
    }
}