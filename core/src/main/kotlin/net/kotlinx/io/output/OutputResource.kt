package net.kotlinx.io.output

import java.io.InputStream
import java.io.OutputStream


/**
 * 스프링하고 비슷함
 * 사실 사용되는건 인풋스트림 직접사용 or 파일 뿐임
 * */
interface OutputResource {

    /** 출력 스트림 */
    val outputStream: OutputStream

    /** 파일 다운로드 등의 쓰기 작업 */
    fun write(inputStream: InputStream) {
        outputStream.use { fileOut ->
            inputStream.copyTo(fileOut, BUFFER_SIZE)
        }
    }

    companion object {
        /**  베이스 버퍼 사이즈  */
        const val BUFFER_SIZE = 4096
    }

}