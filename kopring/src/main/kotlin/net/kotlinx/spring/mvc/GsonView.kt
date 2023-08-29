package net.kotlinx.spring.mvc

import com.google.gson.Gson
import com.lectra.koson.ObjectType
import com.lectra.koson.obj
import com.lectra.koson.rawJson
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.gson.GsonSet
import net.kotlinx.spring.servlet.writeJson
import org.springframework.web.servlet.View

/**
 * 결과 json을 통일 하기 외한 객체
 * 모든 결과는 이걸로 한번 감싸서 리턴된다. (어차피 리플렉션 제한있음)
 * 이 양식은 프로젝트마다 커스텀됨
 * 일반적인 용도로는 사용하지 말것
 * @see SpringGsonConverter
 */
class GsonView(val data: Any, val ok: Boolean = true) : View {

    var gson: Gson = GsonSet.GSON

    override fun render(model: MutableMap<String, *>, request: HttpServletRequest, response: HttpServletResponse) {
        val validJson = when (data) {
            is String -> data
            is GsonData -> data.toString()
            is ObjectType -> data.toString()
            else -> gson.toJson(data)
        }
        response.writeJson(obj {
            "ok" to ok
            "data" to rawJson(validJson)
        })
    }
}