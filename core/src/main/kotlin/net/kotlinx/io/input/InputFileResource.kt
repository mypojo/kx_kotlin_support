package net.kotlinx.io.input

import java.io.File
import java.io.InputStream


/**
 * InputResource 파일용
 * */
data class InputFileResource(val file: File) : InputResource {

    override val inputStream: InputStream
        get() = file!!.inputStream()

}