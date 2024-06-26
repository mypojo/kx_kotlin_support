package net.kotlinx.spring.servlet

import jakarta.servlet.http.HttpServletResponse
import net.kotlinx.json.gson.GsonSet

/** 스프링에 의존적이지 않은 http 예외 클래스 */
data class HttpError(val code: Int, val status: String, val message: String) {

    /** json 으로 바꿔준다 */
    override fun toString(): String = GsonSet.GSON.toJson(this)

}

/** 간단 json 예외 쓰기 */
fun HttpServletResponse.writeError(httpError: HttpError) = this.writeJson(httpError, httpError.code)