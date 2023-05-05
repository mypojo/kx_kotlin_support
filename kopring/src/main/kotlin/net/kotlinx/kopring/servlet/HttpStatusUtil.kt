package net.kotlinx.kopring.servlet

import org.springframework.http.HttpStatus

/** 자주 사용되는 spring http 상태 모음 */
object HttpStatusUtil {

    /** http 에러 */
    fun httpError(httpStatus: HttpStatus, msg: String): HttpError = httpStatus.let { HttpError(it.value(), it.name, msg) }

    /** 401 unauthorized = 로그인이 필요할때 */
    fun unauthorized(msg: String): HttpError = httpError(HttpStatus.UNAUTHORIZED, msg)

    /** 403 forbidden = 로그인이 필요할때 */
    fun forbidden(msg: String): HttpError = httpError(HttpStatus.FORBIDDEN, msg)

}
