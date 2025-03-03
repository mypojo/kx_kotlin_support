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
    private fun GsonBuilder.applyDefaut1() {
        setExclusionStrategies(NotExposeStrategy())
        registerTypeAdapter(GsonData::class.java, GsonAdapterUtil.GsonDataAdapter())
        registerTypeAdapter(BigDecimal::class.java, GsonAdapterUtil.BigDecimalAdapter())
    }

    /** 기본 + 기본 시간대 변경 */
    private fun GsonBuilder.applyDefaut2() {
        applyDefaut1()
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
            applyDefaut2()
            setPrettyPrinting()
        }.create()!!
    }

    /**
     * bean(카멜) 을 athena table 데이터 등으로 바로 쓸때 사용
     * 대부분 athena 로 읽으면 대소문자 구분을 안하는 경우가 많아서 UNDERSCORES로 하는게 편함
     * timestamp 가 UTC만 지원하는겨웅 이걸 써야함
     * ex) athena 의 timestamp는 UTC만 지원함.   서울 13시 -> UTC 04시로 입력해야 정상 작동
     *  */
    val TABLE_UTC: Gson by lazy {
        GsonBuilder().apply {
            applyDefaut1()
            setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            registerTypeAdapter(LocalDateTime::class.java, GsonAdapterUtil.DateTimeUtceAdapter(UtcConverter.ISO_INSTANT)) //날짜가 UTC
        }.create()!!
    }

    /**
     * TABLE_UTC 와 동일한데 ZONE 적용 -> 이러면 LocalDateTime으로 자동변환 가능해서 편하다
     * ex) bean 을  kinesis or firehose 에 입력
     * ex) eventBridge 의 event detail 부분을 만들때 사용
     * @see TimeFormat.ISO_OFFSET 참고
     *  */
    val TABLE_UTC_WITH_ZONE: Gson by lazy {
        GsonBuilder().apply {
            applyDefaut1()
            setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            registerTypeAdapter(LocalDateTime::class.java, GsonAdapterUtil.DateTimeUtceAdapter(UtcConverter.ISO_OFFSET)) //날짜가 UTC_ZONE
        }.create()!!
    }

    /**
     * bean 그대로 쓸때 사용
     * 크게 쓸일이 없음..
     *  */
    val BEAN_UTC_WITH_ZONE: Gson by lazy {
        GsonBuilder().apply {
            applyDefaut1()
            registerTypeAdapter(LocalDateTime::class.java, GsonAdapterUtil.DateTimeUtceAdapter(UtcConverter.ISO_OFFSET)) //날짜가 UTC_ZONE
        }.create()!!
    }


}