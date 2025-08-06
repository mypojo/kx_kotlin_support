package net.kotlinx.spring.servlet

import net.kotlinx.json.gson.GsonSet

/** 스프링에 의존적이지 않은 http 예외 클래스 */
data class HttpError(val code: Int, val status: String, val message: String) {

    /** json 으로 바꿔준다 */
    override fun toString(): String = GsonSet.GSON.toJson(this)

}