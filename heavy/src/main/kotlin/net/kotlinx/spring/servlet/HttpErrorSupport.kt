package net.kotlinx.spring.servlet

import jakarta.servlet.http.HttpServletResponse

/** 간단 json 예외 쓰기 */
fun HttpServletResponse.writeError(httpError: HttpError) = this.writeJson(httpError, httpError.code)