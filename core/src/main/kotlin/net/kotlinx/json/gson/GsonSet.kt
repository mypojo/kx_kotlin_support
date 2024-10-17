package net.kotlinx.json.gson

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.kotlinx.time.TimeFormat
import net.kotlinx.time.UtcConverter
import java.math.BigDecimal
import java.time.LocalDateTime


/**
 * 참고용 간단 모음집
 *
 * 아래와 같은 사용법 있음
 * setDateFormat("xxx)
 * gsonBuilder.excludeFieldsWithModifiers(Modifier.STATIC,Modifier.TRANSIENT);
 */
object GsonSet {

    /** 기본적인것들 세팅 */
    private fun GsonBuilder.applyDefaut() {
        setExclusionStrategies(NotExposeStrategy())
        registerTypeAdapter(GsonData::class.java, GsonAdapterUtil.GsonDataAdapter())
        registerTypeAdapter(BigDecimal::class.java, GsonAdapterUtil.BigDecimalAdapter())
    }

    private fun GsonBuilder.applyDefaut2() {
        applyDefaut()
        registerTypeAdapter(LocalDateTime::class.java, GsonAdapterUtil.DateTimeAdapter(TimeFormat.YMDHMS)) //날짜만 변경해줌
        registerTypeAdapter(Map::class.java, GsonAdapterUtil.MapAdapter()) //Lambda 기본 변환에 사용 (다른데는 쓸일 없음)
    }

    /**
     * 디폴트 변환용
     * date 기반의 기본조건(setDateFormat) 안씀
     *  */
    val GSON: Gson by lazy {
        GsonBuilder().apply {
            applyDefaut2()
        }.create()!!
    }

    /**
     * GSON 하고 동일한데 UNDERSCORES 버전
     * ex) athenm 결과 vo로 매핑
     *  */
    val GSON_UNDERSCORES: Gson by lazy {
        GsonBuilder().apply {
            applyDefaut2()
            setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        }.create()!!
    }

    /** 이쁘게 */
    val GSON_PRETTY: Gson by lazy {
        GsonBuilder().apply {
            applyDefaut()
            setPrettyPrinting()
        }.create()!!
    }

    /**
     * 이하 적용된것
     * AWS kinesis 입력데이터 ( athena table 데이터)
     *  */
    val TABLE_UTC: Gson by lazy {
        GsonBuilder().apply {
            applyDefaut()
            setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            registerTypeAdapter(LocalDateTime::class.java, GsonAdapterUtil.DateTimeUtceAdapter(UtcConverter.ISO_INSTANT)) //날짜가 UTC
        }.create()!!
    }

    /**
     * 이하 적용된것
     * eventBridge event 적용
     *  */
    val BEAN_UTC_ZONE: Gson by lazy {
        GsonBuilder().apply {
            applyDefaut()
            registerTypeAdapter(LocalDateTime::class.java, GsonAdapterUtil.DateTimeUtceAdapter(UtcConverter.ISO_OFFSET)) //날짜가 UTC_ZONE
        }.create()!!
    }


}