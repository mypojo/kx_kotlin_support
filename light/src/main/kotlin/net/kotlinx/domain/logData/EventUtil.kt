package net.kotlinx.domain.logData

import mu.KotlinLogging
import net.kotlinx.exception.toSimpleString

/**
 * 페이로드 크기: 64KB 청크의 각 페이로드가 1개의 이벤트로 청구 -> 예를 들어 256KB 페이로드의 이벤트는 4개의 요청으로 청구
 * 일반적인 단순 메세지는 1000 byte 정도 함
 */
@Deprecated("LogData 사용하세요")
object EventUtil {

    private val log = KotlinLogging.logger {}

    /** 에러를 request 객체에 임시로 담기 위한 키값 */
    const val ERROR = "error"

    /** 이벤트 기록 공통. 여기서 발생하는 오류는 일단 무시한다. */
    fun doWithoutException(event: () -> Unit) {
        try {
            event()
        } catch (e: Exception) {
            log.warn { "이벤트 기록중 예외발생 : ${e.toSimpleString()}" }
        }
    }
}