package net.kotlinx.io.input

import java.io.InputStream


/**
 * InputResource inputStream용
 * */
data class InputDefaultResource(override val inputStream: InputStream) : InputResource