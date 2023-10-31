package net.kotlinx.core.gson

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.kotlinx.core.time.TimeFormat
import java.time.LocalDateTime


/**
 * 참고용 간단 모음집
 *
 * 아래와 같은 사용법 있음
 * setDateFormat("xxx)
 * gsonBuilder.excludeFieldsWithModifiers(Modifier.STATIC,Modifier.TRANSIENT);
 */
object GsonSet {

    /**
     * 디폴트 변환용
     * date 기반의 기본조건(setDateFormat) 안씀
     *  */
    val GSON: Gson by lazy {
        GsonBuilder().apply {
            setExclusionStrategies(NotExposeStrategy())
            registerTypeAdapter(Map::class.java, GsonAdapterUtil.MapAdapter()) //Lambda 기본 변환에 사용 (다른데는 쓸일 없음)
            registerTypeAdapter(LocalDateTime::class.java, GsonAdapterUtil.DateTimeAdapter(TimeFormat.YMDHMS)) //날짜만 변경해줌
        }.create()!!
    }

    /** 이쁘게 */
    val GSON_PRETTY: Gson by lazy {
        GsonBuilder().apply {
            setExclusionStrategies(NotExposeStrategy())
            setPrettyPrinting()
        }.create()!!
    }

    /**
     * 이하 적용된것
     * AWS kinesis 입력데이터 ( athena table 데이터)
     *  */
    val TABLE_UTC: Gson by lazy {
        GsonBuilder().apply {
            setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            setExclusionStrategies(NotExposeStrategy())
            registerTypeAdapter(LocalDateTime::class.java, GsonAdapterUtil.DateTimeUtcAdapter()) //날짜만 변경해줌
            registerTypeAdapter(GsonData::class.java, GsonAdapterUtil.GsonDataAdapter())
        }.create()!!
    }

    /**
     * 이하 적용된것
     * eventBridge event 적용
     *  */
    val BEAN_UTC_ZONE: Gson by lazy {
        GsonBuilder().apply {
            setExclusionStrategies(NotExposeStrategy())
            registerTypeAdapter(LocalDateTime::class.java, GsonAdapterUtil.DateTimeUtcZoneAdapter()) //날짜만 변경해줌
            registerTypeAdapter(GsonData::class.java, GsonAdapterUtil.GsonDataAdapter())
        }.create()!!
    }


}