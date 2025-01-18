package net.kotlinx.json.gson

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.kotlinx.regex.RegexSet
import net.kotlinx.string.retainFrom
import net.kotlinx.time.TimeFormat
import net.kotlinx.time.UtcConverter
import java.io.IOException
import java.lang.reflect.Type
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.Throws

/**
 * GSON 코드 샘플 모음
 * 3가지 어뎁터를 지원한다.
 * 1. registerTypeAdapter : 단독 클래스에 매핑
 * 2. registerTypeHierarchyAdapter : 하위 객체에 매핑 (주로 인터페이스)
 * 3. registerTypeAdapterFactory : 어노테이션을 활용헌 커스텀 매퍼 (중요!!)
 *
 * 아래 어뎁터를 지원한다.
 * 1. TypeAdapter : 단순 변환
 * 2. JsonDeserializer / JsonSerializer : 정확한 타입토큰을 지원하는 어뎁터.  Type 을 넘겨받는다.
 * 3. InstanceCreator : 안써봄
 */
object GsonAdapterUtil {

    //==================================================== 커스텀 객체 ======================================================

    /** GsonData */
    class GsonDataAdapter : JsonDeserializer<GsonData>, JsonSerializer<GsonData> {

        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): GsonData {
            return GsonData(json)
        }

        override fun serialize(src: GsonData, type: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src.delegate)
        }
    }

    //==================================================== 숫자 관련 (변환 에러 줄이는 용도) : 대부분의 문제가 숫자 변환에서 생김 ======================================================

    /** 기본 변환 */
    class BigDecimalAdapter : JsonDeserializer<BigDecimal>, JsonSerializer<BigDecimal?> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): BigDecimal {
            return context.deserialize<String>(json, String::class.java).toBigDecimal()
        }

        override fun serialize(src: BigDecimal?, type: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src?.toPlainString()) //scale 을 -1 로 잡으면 1.2E+2 이런식으로 표시되는것을 방지
        }
    }

    class LongAdapter : JsonDeserializer<Long>, JsonSerializer<Long?> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): Long = json.asLong
        override fun serialize(src: Long?, type: Type, context: JsonSerializationContext): JsonElement = JsonPrimitive(src)
    }

    class IntAdapter : JsonDeserializer<Int>, JsonSerializer<Int?> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): Int = json.asInt
        override fun serialize(src: Int?, type: Type, context: JsonSerializationContext): JsonElement = JsonPrimitive(src)
    }

    /**
     * 이거 안쓰면 Int/Long 이 Double 처럼 변환됨
     * */
    class MapAdapter : JsonDeserializer<Map<String, Any>>, JsonSerializer<Map<String, Any>?> {

        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Map<String, Any> {
            return mutableMapOf<String, Any>().apply {
                json.asJsonObject.asMap().entries.forEach { e ->
                    val deserialize = context!!.deserialize<Any>(e.value, e.value::class.java) //내부 객체도 컨텍스트 통과
                    put(e.key, deserialize)
                }
            }
        }

        override fun serialize(src: Map<String, Any>?, type: Type, context: JsonSerializationContext): JsonElement {
            if (src == null) return JsonNull.INSTANCE
            return JsonObject().apply {
                src.entries.forEach { e ->
                    val jsonElement = context.serialize(e.value)
                    add(e.key, jsonElement)
                }
            }
        }
    }

    //==================================================== 시간 관련 (기본 date 베이스) ======================================================

    /** 타임포맷 지정되는 어댑터 */
    class DateTimeAdapter(val timeFormat: TimeFormat) : JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): LocalDateTime = timeFormat.toLocalDateTime(json.asString)
        override fun serialize(src: LocalDateTime?, type: Type, context: JsonSerializationContext): JsonElement = JsonPrimitive(src?.let { timeFormat[src] } ?: "")
    }

    /** 시간의 경우 UTC 기본값으로 변환해줌 */
    class DateTimeUtceAdapter(private val converter: UtcConverter) : JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {

        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): LocalDateTime =
            converter.fromText(json.asString)

        override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
            JsonPrimitive(src?.let { converter.toText(it) } ?: "")
    }


    //==================================================== 외부 주요 객체 ======================================================

    /** 정확한 타입토큰을 지원하는 어뎁터  */
    class AtomicLongAdapter : JsonDeserializer<AtomicLong>, JsonSerializer<AtomicLong> {
        override fun serialize(src: AtomicLong, arg1: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src.get())
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): AtomicLong {
            return AtomicLong(json.asLong)
        }
    }

//    //==================================================== 깡 어뎁터 샘플. 컴파일 시점에 정의되어있어야 한다. (JsonAdapter 용) ======================================================


    /** 마스킹 샘플  */
    class Mask01Adapter : TypeAdapter<String>() {

        @Throws(IOException::class)
        override fun read(`in`: JsonReader): String {
            val string = `in`.nextString()
            return string.trim { it <= ' ' }
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: String) {
            writer.value("**" + value.substring(2))
        }
    }

    /**
     * 전화번호 입력
     */
    class StringHpAdapter : TypeAdapter<String>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): String {
            var string = reader.nextString().trim { it <= ' ' }
            //안드로이드에서 입력되는것을 한국?식으로 바꿔준다.
            if (string.startsWith("+82")) {
                string = string.replaceFirst("\\+82".toRegex(), "0")
            }
            //csv 등에서 입력시 숫자형이라 앞에 0이 짤리는 현상을 막아준다.
            if (string.startsWith("10")) {
                string = "0$string"
            }
            return string.retainFrom(RegexSet.NUMERIC)
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: String) {
            writer.value(value)
        }
    }

    /** 소문자로 치환 입력  */
    class StringToLowerAdapter : TypeAdapter<String>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): String {
            val string = reader.nextString()
            return string.trim { it <= ' ' }.lowercase(Locale.getDefault())
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: String) {
            writer.value(value)
        }
    }
}
