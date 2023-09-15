package net.kotlinx.aws.lambda

import com.google.gson.JsonElement
import com.lectra.koson.ObjectType
import net.kotlinx.aws.AwsNaming
import net.kotlinx.core.gson.GsonData

/** 람다결과(map) 형식 지원하는 객체 */
interface LambdaMapResult {

    fun toLambdaMap(): Map<String, Any>

}


object LambdaHandlerUtil {

    /**
     * 람다 응답객체로 리턴
     * 오리지날 자바 응답의 경우 시리얼라이즈 되야하기 때문에 범용적인 map을 사용한다.
     * map 중첩으로 json을 구현함
     *  */
    fun anyToLambdaMap(handlerResp: Any): Map<String, Any> = when (handlerResp) {
        //==================================================== 일반 문자열은 그냥 바디로 ======================================================
        is String -> mapOf(AwsNaming.BODY to handlerResp)
        //==================================================== json ======================================================
        is ObjectType -> GsonData.parse(handlerResp).fromJson<LinkedHashMap<String, Any>>()
        is GsonData -> handlerResp.fromJson<LinkedHashMap<String, Any>>()
        is JsonElement -> GsonData(handlerResp).fromJson<LinkedHashMap<String, Any>>()
        //==================================================== 객체 ======================================================
        is LambdaMapResult -> handlerResp.toLambdaMap()
        else -> GsonData.fromObj(handlerResp).fromJson<LinkedHashMap<String, Any>>()
    }


}


