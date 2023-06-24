package net.kotlinx.spring.mvc

import com.google.gson.Gson
import net.kotlinx.core.gson.GsonSet
import net.kotlinx.core.string.ResultData
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter
import java.io.Reader
import java.io.Writer
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * gson 결과를 래핀해준다
 * */
class SpringGsonConverter(
    val gson: Gson = GsonSet.GSON,
    val block: (Any) -> Any = { ResultData(true, it) },
) : AbstractJsonHttpMessageConverter() {

    /** 그대로 변환 */
    override fun readInternal(resolvedType: Type, reader: Reader): Any {
        return gson.fromJson(reader, resolvedType)
    }

    /** 템플릿으로 한번 감싸서 리턴한다 */
    override fun writeInternal(obj: Any, type: Type, writer: Writer) {
        val converted = block(obj)
        if (type is ParameterizedType) {
            gson.toJson(converted, type, writer)
        } else {
            gson.toJson(converted, writer)
        }
    }


}