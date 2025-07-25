package net.kotlinx.io.input

import java.io.ByteArrayInputStream
import java.io.InputStream


/**
 * InputResource inputStream용
 * */
data class InputDefaultResource(override val inputStream: InputStream) : InputResource {

    /** 문자열로 스트림을 구성하는 간단 샘플 */
    fun fromString(text: String) = InputDefaultResource(ByteArrayInputStream(text.toByteArray()))

}