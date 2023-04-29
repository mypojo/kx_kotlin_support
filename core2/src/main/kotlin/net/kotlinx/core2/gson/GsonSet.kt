package net.kotlinx.core2.gson

import com.google.gson.*
import net.kotlinx.core1.time.fromUtc
import net.kotlinx.core1.time.toUtc
import java.lang.reflect.Type
import java.time.LocalDateTime


/**
 * 참고용 간단 모음집
 */
object GsonSet {

    /** 디폴트 변환용*/
    val GSON by lazy {
        GsonBuilder().apply {
            setExclusionStrategies(NotExposeStrategy())
        }.create()!!
    }

    /** 이쁘게 */
    val GSON_PRETTY by lazy {
        GsonBuilder().apply {
            setExclusionStrategies(NotExposeStrategy())
            setPrettyPrinting()
        }.create()!!
    }


    /** AWS kinesis 등에 연동할 데이터 변환용 */
    val TABLE_UTC by lazy {
        GsonBuilder().apply {
            //setDateFormat("xxx)
            setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            setExclusionStrategies(NotExposeStrategy())
            //gsonBuilder.excludeFieldsWithModifiers(Modifier.STATIC,Modifier.TRANSIENT);
            registerTypeAdapter(LocalDateTime::class.java, DateTimeUtcAdapter()) //날짜만 변경해줌
        }.create()!!
    }

    /** 시간의 경우 UTC 기본값으로 변환해줌 */
    class DateTimeUtcAdapter : JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): LocalDateTime = json.asString.fromUtc()
        override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement = JsonPrimitive(src?.toUtc() ?: "")
    }

}