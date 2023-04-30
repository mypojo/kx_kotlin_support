package net.kotlinx.kopring.spring.resource

import org.springframework.core.io.Resource
import org.springframework.core.io.WritableResource
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.net.URL

/**
 * file 없이 outputStream 만 있는 리소스
 * 없길래 걍 하나 만들었다. file 있으면 File 리소스 쓸것.
 */
data class OutputStreamResource2(
    private val outputStream: OutputStream,
) : WritableResource {

    /** 이거 하나 때문에.. */
    override fun getOutputStream(): OutputStream = outputStream

    override fun contentLength(): Long = throw UnsupportedOperationException()

    override fun createRelative(arg0: String): Resource = throw UnsupportedOperationException()

    override fun exists(): Boolean = true

    override fun getDescription(): String = throw UnsupportedOperationException()

    override fun getFile(): File = throw UnsupportedOperationException()

    override fun getFilename(): String = throw UnsupportedOperationException()

    override fun getURI(): URI = throw UnsupportedOperationException()

    override fun getURL(): URL = throw UnsupportedOperationException()

    override fun isOpen(): Boolean = throw UnsupportedOperationException()

    override fun isReadable(): Boolean = false

    override fun lastModified(): Long = throw UnsupportedOperationException()

    override fun getInputStream(): InputStream = throw UnsupportedOperationException()

    override fun isWritable(): Boolean = true

}
