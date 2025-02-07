package net.kotlinx.io.output

import java.io.OutputStream


/**
 * OutputResource OutputStream용
 * */
data class OutputDefaultResource(override val outputStream: OutputStream) : OutputResource