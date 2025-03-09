package net.kotlinx.io.output

import java.io.File
import java.io.OutputStream


//==================================================== 간단 변환들 ======================================================

/** 간단변환 */
fun OutputStream.toOutputResource(): OutputDefaultResource = OutputDefaultResource(this)

/** 간단변환 */
fun File.toOutputResource(writerGzip: Boolean = false): OutputFileResource = OutputFileResource(this, writerGzip)

