package net.kotlinx.spring.servlet

import aws.smithy.kotlin.runtime.text.encoding.encodeBase64
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import net.kotlinx.csv.CsvUtil
import net.kotlinx.string.encodeUrl
import org.aspectj.weaver.tools.cache.SimpleCacheFactory
import org.springframework.http.MediaType
import kotlin.time.Duration.Companion.days


/**
 * Poi처럼 resp에 write만 있는 경우 파일이름 등을 지정하기 위해 사용
 * 파일 크기는 생략함 -> resp.setContentLength((int) file.length());
 *  */
fun HttpServletResponse.setFileName(fileName: String, contentType: String = MediaType.APPLICATION_OCTET_STREAM_VALUE) {
    this.contentType = contentType
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

/**
 * 간단 csv 쓰기 (한글)
 * 이스케이핑 안함 주의!!
 * ex) 각종 템플릿류
 *  */
fun HttpServletResponse.writeCsvLines(fileName: String, lines: List<List<String>>) {
    setFileName(fileName, "text/csv;charset=MS949")
    CsvUtil.ms949Writer().writeAll(lines, this.outputStream)
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

