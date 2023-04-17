package net.kotlinx.kopring.spring

import org.springframework.core.io.InputStreamResource
import java.io.InputStream

/**
 * 생성할때 메타데이터를 가지고 시작하는 리소스
 * 다운로드시 file에 쓰지 않고, 직접 읽을때 사용
 */
data class InputStreamResource2(
    private val inputStream: InputStream,
    private val contentLength: Long,
) : InputStreamResource(inputStream) {

    override fun contentLength(): Long = contentLength

}
