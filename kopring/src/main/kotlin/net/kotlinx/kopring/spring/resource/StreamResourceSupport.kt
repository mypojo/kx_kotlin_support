package net.kotlinx.kopring.spring.resource

import org.springframework.core.io.InputStreamResource
import java.io.InputStream
import java.io.OutputStream

/** 없길래 추가 */
fun InputStream.toResource(): InputStreamResource = InputStreamResource(this)

/** 없길래 추가 */
fun OutputStream.toResource(): OutputStreamResource2 = OutputStreamResource2(this)
