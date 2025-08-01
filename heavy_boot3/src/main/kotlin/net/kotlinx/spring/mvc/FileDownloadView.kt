package net.kotlinx.spring.mvc

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.kotlinx.spring.servlet.setFileName
import org.springframework.web.servlet.View
import java.io.File

/**
 * 파일 다운로드 & 삭제
 * 이렇게 하지 말고 S3를 직접 사용하는것을 권장함
 * view 타입을 명시적으로 구분하기위해서 익명 클래스 사용 안함
 * -> 부트의 경우 ResponseEntity를 쓰세요
 * */
@Deprecated("s3 프리사인 사용하세요")
class FileDownloadView(
    val file: File,
    val deleteAfter: Boolean = true
) : View {

    override fun render(model: MutableMap<String, *>?, request: HttpServletRequest, response: HttpServletResponse) {
        response.setFileName(file.name)
        response.setContentLengthLong(file.length())
        file.inputStream().use { ins ->
            response.outputStream.use { out ->
                ins.copyTo(out)
            }
        }
        if (deleteAfter) {
            file.delete()
        }
    }
}