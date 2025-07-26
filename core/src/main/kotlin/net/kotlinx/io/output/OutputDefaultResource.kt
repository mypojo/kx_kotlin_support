package net.kotlinx.io.output

import java.io.OutputStream


/**
 * OutputResource 의 기본 구현체
 * */
data class OutputDefaultResource(override val outputStream: OutputStream) : OutputResource