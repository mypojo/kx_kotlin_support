package net.kotlinx.ai

import net.kotlinx.string.decodeUrl
import java.io.File

/**
 * 간단 입력 인터페이스
 * 여기 해당하지 않는건 다 단일 텍스트로 간주한다.
 *
 * 베드락 : 모두 리스트로 간주
 * 오픈AI : 싱글 or 리스트
 * */
interface AiTextInput {

    /** 파일 입력시 플랫폼에따라 둘중 하나가 필요함 */
    data class AiTextInputFile(
        val file: File? = null,
        val url: String? = null,
    ) : AiTextInput {

        val name: String
            get() {
                if (file != null) return file.name
                if (url != null) return url.decodeUrl().substringAfterLast("/")
                return "-"
            }

    }

}