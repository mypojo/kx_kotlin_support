package net.kotlinx.io.input

import java.io.File
import java.io.InputStream


//==================================================== 간단 변환들 ======================================================

/** 간단변환 */
fun InputStream.toInputResource(): InputDefaultResource = InputDefaultResource(this)

/** 간단변환 */
fun File.toInputResource(): InputFileResource = InputFileResource(this)

