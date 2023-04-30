package net.kotlinx.kopring.spring.resource

import org.springframework.core.io.InputStreamResource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


fun File.toGzipOutputStreamResource(): OutputStreamResource2 = OutputStreamResource2(GZIPOutputStream(FileOutputStream(this)))

fun File.toGzipInputStreamResource(): InputStreamResource = InputStreamResource(GZIPInputStream(FileInputStream(this)))