package net.kotlinx.kopring.servlet

import net.kotlinx.core1.string.encodeUrl
import org.springframework.http.MediaType
import javax.servlet.http.HttpServletResponse


/** Poi처럼 resp에 write만 있는 경우 파일이름 등을 지정하기 위해 사용  */
fun HttpServletResponse.setFileName(fileName: String, contentType: String = MediaType.APPLICATION_OCTET_STREAM_VALUE) {
    this.contentType = contentType
    //resp.setContentLength((int) file.length());
    this.setHeader("Content-Disposition", "attachment; fileName=\"${fileName.encodeUrl()}\";") // 한글 파일명 확인 필요
    this.setHeader("Content-Transfer-Encoding", "binary")
}
