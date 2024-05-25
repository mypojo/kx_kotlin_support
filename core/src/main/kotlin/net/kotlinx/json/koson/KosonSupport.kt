package net.kotlinx.json.koson

import com.lectra.koson.*
import net.kotlinx.json.gson.GsonData

//==================================================== block: Koson.() -> Unit  =>  NotionPageBlockClient 참고 ======================================================

/** 이런식으로 변환 가능하다. list map 할때 참고 */
fun List<KosonType>.toKsonArray(): ArrayType = arr[this]

/** 간단 변환 */
fun KosonType.toGsonData(): GsonData = GsonData.parse(this)

/**
 * koson에 입력할때, KosonType을 받는 infix가 없음.. (왜없지? ㅠㅠ)
 * 내부 infix에  추가하는건 불가능하기때문에 옵션을 하나 더 만들어줌
 * 더 깔끔한 방법을 찾고싶다..
 * */
fun Koson.rawKeyValue(name: String, koson: Any) {
    when (koson) {
        is ArrayType -> name to koson
        is ObjectType -> name to koson
        is String -> name to koson
        is Number -> name to koson
        is Boolean -> name to koson
        is GsonData -> name to rawJson(koson.toString())
        else -> throw IllegalStateException()
    }
}