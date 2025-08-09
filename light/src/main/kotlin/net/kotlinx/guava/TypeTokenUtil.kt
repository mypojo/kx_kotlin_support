package net.kotlinx.guava

import com.google.common.collect.Multimap
import com.google.common.reflect.TypeToken
import java.lang.reflect.Type

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
@Suppress("UNCHECKED_CAST")
object TypeTokenUtil {

    /** List<T> 타입 리턴  </T> */
    inline fun <reified T> list(): Type =
        object : TypeToken<List<T>>() {}.type

    /**
     * Map<String></String>, Collectio<T>> 인 타입 리턴
    </T> */
    inline fun <reified T> mapCollectionType(): Type =
        object : TypeToken<Map<String, Collection<T>>>() {}.type

    /**
     * Map<String></String>, T> 인 타입 리턴
     * 경고 줄이고 인라인으로 만들기 위해 도입
     */
    inline fun <reified T> mapType(): Type =
        object : TypeToken<Map<String, T>>() {}.type

    /**
     * Map<String></String>, Map<String></String>,T>> 인 타입 리턴
     */
    inline fun <reified T> mapMapType(): Type =
        object : TypeToken<Map<String, Map<String, T>>>() {}.type

    /**
     * Map<String></String>, Map<String></String>,Map<String></String>,T>>> 인 타입 리턴
     */
    inline fun <reified T> mapMapMapType(): Type =
        object : TypeToken<Map<String, Map<String, Map<String, T>>>>() {}.type

    /**
     * Map<String></String>, Map<String></String>,Map<String></String>,T>>> 인 타입 리턴
     */
    inline fun <reified T> mapMapMapMapType(): Type =
        object : TypeToken<Map<String, Map<String, Map<String, Map<String, T>>>>>() {}.type

    /**
     * Multimap<String></String>, T> 인 타입 리턴
     * ex) TypeTokenUtil.multimapType(Long.class) => com.google.common.collect.Multimap<java.lang.String></java.lang.String>, java.lang.Long>
     */
    inline fun <reified T> multimapType(): Type =
        object : TypeToken<Multimap<String, T>>() {}.type
}
