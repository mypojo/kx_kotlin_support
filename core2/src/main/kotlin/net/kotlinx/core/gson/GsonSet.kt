package net.kotlinx.core.gson

import com.google.gson.*
import net.kotlinx.core.time.TimeFormat
import net.kotlinx.core.time.fromUtc
import net.kotlinx.core.time.toUtc
import java.lang.reflect.Type
import java.time.LocalDateTime


/**
 * 참고용 간단 모음집
 */
object GsonSet {

    /**
     * 디폴트 변환용
     * date 기반의 기본조건(setDateFormat) 안씀
     *  */
    val GSON by lazy {
        GsonBuilder().apply {
            setExclusionStrategies(NotExposeStrategy())
//            registerTypeAdapter(Long::class.java, GsonAdapterUtil.LongAdapter())
//            registerTypeAdapter(Int::class.java, GsonAdapterUtil.IntAdapter())
            registerTypeAdapter(Map::class.java, GsonAdapterUtil.MapAdapter()) //Lambda 기본 변환에 사용 (다른데는 쓸일 없음)
            registerTypeAdapter(LocalDateTime::class.java, GsonAdapterUtil.DateTimeAdapter(TimeFormat.YMDHMS)) //날짜만 변경해줌
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