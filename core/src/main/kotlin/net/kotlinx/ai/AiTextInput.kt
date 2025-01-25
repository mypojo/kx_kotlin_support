package net.kotlinx.ai

import net.kotlinx.core.Kdsl
import net.kotlinx.string.decodeUrl
import java.io.File

/**
 * 간단 입력 인터페이스
 * 여기 해당하지 않는건 다 단일 텍스트로 간주한다.
 * 한번의 요청(메세지)에 다수의 파일이나 문구(컨텐츠)가 들어갈 수 있다
 *
 * 베드락 : 모두 리스트로 간주
 * 오픈AI : 싱글 or 리스트
 * */
sealed interface AiTextInput {

    /** 단순 문자열 */
    data class AiTextString(val text: String) : AiTextInput

    /** 이미지 입력시 플랫폼에따라 둘중 하나가 필요함 */
    class AiTextImage : AiTextInput {

        @Kdsl
        constructor(block: AiTextImage.() -> Unit = {}) {
            apply(block)
        }

        /** 배드록은 파일의 바이트가 필요함 */
        var file: File? = null

        /** 배드록은 파일의 바이트가 필요함 */
        var fileBody: ByteArray? = null

        val byteArray: ByteArray
            get() {
                return when {
                    fileBody != null -> fileBody!!
                    file != null -> file!!.readBytes()
                    else -> throw IllegalStateException("배드락은 파일 바이트가 필수입니다")
                }
            }

        /** GPT는 URL 링크가 필요함 */
        var url: String? = null

        val name: String
            get() {
                return when {
                    file != null -> file!!.name
                    url != null -> url!!.decodeUrl().substringAfterLast("/")
                    else -> "-"
                }
            }

    }

}