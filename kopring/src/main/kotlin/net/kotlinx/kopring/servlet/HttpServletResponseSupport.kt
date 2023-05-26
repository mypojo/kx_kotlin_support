package net.kotlinx.kopring.servlet

import aws.smithy.kotlin.runtime.util.encodeBase64
import net.kotlinx.core.string.encodeUrl
import org.aspectj.weaver.tools.cache.SimpleCacheFactory
import org.springframework.http.MediaType
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import kotlin.time.Duration.Companion.days


/** Poi처럼 resp에 write만 있는 경우 파일이름 등을 지정하기 위해 사용  */
fun HttpServletResponse.setFileName(fileName: String, contentType: String = MediaType.APPLICATION_OCTET_STREAM_VALUE) {
    this.contentType = contentType
    //resp.setContentLength((int) file.length());
    this.setHeader("Content-Disposition", "attachment; fileName=\"${fileName.encodeUrl()}\";") // 한글 파일명 확인 필요
    this.setHeader("Content-Transfer-Encoding", "binary")
}

/** 간단 json 쓰기 */
fun HttpServletResponse.writeJson(json: Any, httpStatus: Int = 200) {
    this.contentType = "application/json"
    this.characterEncoding = "UTF-8" //이거 안하면 한글 깨짐
    this.status = httpStatus
    this.writer.use {
        it.write(json.toString())
    }
}

//==================================================== 쿠키 시리즈 ======================================================

fun HttpServletResponse.addCookie(key: String, vlaue: Any, maxAge: Long = 1.days.inWholeSeconds) {
    val cookie = Cookie(key, vlaue.toString().encodeBase64()).apply {
        this.maxAge = maxAge.toInt()
        this.path = SimpleCacheFactory.path
    }
    this.addCookie(cookie)
}

/** maxAge를 조절해주는게 아니라 없는걸 추가해준다.  */
fun HttpServletResponse.removeCookie(key: String) {
    this.addCookie(key, "", 0)
}

