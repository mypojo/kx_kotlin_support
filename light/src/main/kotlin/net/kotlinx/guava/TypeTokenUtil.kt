package net.kotlinx.guava

import com.google.common.collect.Multimap
import com.google.common.reflect.TypeParameter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.gson.GsonSet
import java.lang.reflect.Type

/** json을 List 객체로 간단 변환 */
inline fun <reified T> Gson.fromJsonList(json: String): List<T> {
    val type = TypeTokenUtil.list<T>(T::class.java)
    return this.fromJson(json, type)
}

/** json을 List 객체로 간단 변환 */
inline fun <reified T> GsonData.fromJsonList(gson: Gson = GsonSet.GSON): List<T> = gson.fromJsonList<T>(this.toString())

/**
 * 구아바에서 지원하는 리플렉션 상세 도구
 * JSON 컨버팅 등에 사용된다.
 * https://www.baeldung.com/kotlin/gson-typetoken
 *
 * 참고로
 * vo를 매핑하는 경우는 내부에 제너릭 정보가 있어서 List<T>를 쉽게 변환 가능하지만
 * List<T> 를 직접 매핑하는 경우는 제너릭 정보가 없기때문에 타입을ㅇ 지정해주어야 한다.
 *
 * 실무용 + 소스 참고용
</T></T> */
object TypeTokenUtil {

    /** List<T> 타입 리턴  </T> */
    fun <T> list(type: Type): Type {
        return object : TypeToken<List<T>?>() {}.where(object : TypeParameter<T>() {}, TypeToken.of(type) as TypeToken<T>).type
    }

    /**
     * Map<String></String>, Collectio<T>> 인 타입 리턴
    </T> */
    fun <T> mapCollectionType(type: Type?): Type {
        return if (type == null) MutableMap::class.java else object :
            TypeToken<Map<String?, Collection<T>?>?>() {}.where(object :
            TypeParameter<T>() {}, TypeToken.of(type) as TypeToken<T>).type //파라메터가 없는 타입이라면 일반 MAP리턴
    }

    /**
     * Map<String></String>, T> 인 타입 리턴
     * 경고 줄이고 인라인으로 만들기 위해 도입
     */
    fun <T> mapType(type: Type?): Type {
        return if (type == null) MutableMap::class.java else object : TypeToken<Map<String?, T>?>() {}.where(object :
            TypeParameter<T>() {}, TypeToken.of(type) as TypeToken<T>).type //파라메터가 없는 타입이라면 일반 MAP리턴
    }

    /**
     * Map<String></String>, Map<String></String>,T>> 인 타입 리턴
     */
    fun <T> mapMapType(type: Type?): Type {
        return if (type == null) MutableMap::class.java else object :
            TypeToken<Map<String?, Map<String?, T>?>?>() {}.where(object :
            TypeParameter<T>() {}, TypeToken.of(type) as TypeToken<T>).type //파라메터가 없는 타입이라면 일반 MAP리턴
    }

    /**
     * Map<String></String>, Map<String></String>,Map<String></String>,T>>> 인 타입 리턴
     */
    fun <T> mapMapMapType(type: Type?): Type {
        return if (type == null) MutableMap::class.java else object :
            TypeToken<Map<String?, Map<String?, Map<String?, T>?>?>?>() {}.where(
            object : TypeParameter<T>() {},
            TypeToken.of(type) as TypeToken<T>
        ).type
    }

    /**
     * Map<String></String>, Map<String></String>,Map<String></String>,T>>> 인 타입 리턴
     */
    fun <T> mapMapMapMapType(type: Type?): Type {
        return if (type == null) MutableMap::class.java else object :
            TypeToken<Map<String?, Map<String?, Map<String?, Map<String?, T>?>?>?>?>() {}.where(
            object : TypeParameter<T>() {},
            TypeToken.of(type) as TypeToken<T>
        ).type
    }

    /**
     * Multimap<String></String>, T> 인 타입 리턴
     * ex) TypeTokenUtil.multimapType(Long.class) => com.google.common.collect.Multimap<java.lang.String></java.lang.String>, java.lang.Long>
     */
    fun <T> multimapType(type: Type?): Type {
        return object : TypeToken<Multimap<String?, T>?>() {}.where(object : TypeParameter<T>() {}, TypeToken.of(type) as TypeToken<T>).type
    }
}
