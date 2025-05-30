package net.kotlinx.json.gson

import com.lectra.koson.Koson
import com.lectra.koson.obj
import net.kotlinx.json.koson.toGsonData

/** 간단 변환. 없으면 빈거 리턴 */
fun String?.toGsonDataOrEmpty(): GsonData {
    if (this.isNullOrBlank()) return GsonData.empty()
    return this.toGsonData()
}

/** 간단 변환 */
fun String.toGsonData(): GsonData = GsonData.parse(this)

/** 간단 변환 */
fun List<GsonData>.toGsonArray(): GsonData = GsonData.array().also { ar -> this.forEach { ar.add(it) } }

/**
 * 간단 변환을 문자열로 변경함
 * ex) athena array<string> 으로 캐스팅
 *  */
fun List<GsonData>.toGsonArrayAsStr(): GsonData = GsonData.array().also { ar -> this.forEach { ar.add(it.toString()) } }


/**
 * 라인을 Gson 으로 변경해줌
 * ex) 헤더가 포함된 CSV를 읽어서 json으로 변경
 * @param header 지정된 헤더값
 * @see toGsonArray
 * */
fun List<List<String>>.toGsonDataWithHeader(header: List<String>): List<GsonData> {
    return this.map { data ->
        GsonData.obj {
            header.forEachIndexed { i, key -> put(key, data[i]) }
        }
    }
}

/** 헤더가 있는 전체 CSV 간단버전 */
fun List<List<String>>.toGsonDataWithHeader(): List<GsonData> {
    val header = this.take(1)[0]
    val lines = this.drop(1)
    return lines.toGsonDataWithHeader(header)
}


/**
 * koson 형태를 유지하면서, 결과를 GsonData로 리턴해준다
 * 객체가 제일 자주 사용되니 이름은 이걸로 지정
 * */
fun json(block: Koson.() -> Unit): GsonData = obj(block).toGsonData()