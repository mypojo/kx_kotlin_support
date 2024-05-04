package net.kotlinx.aws.s3

import net.kotlinx.core.file.slash
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.file.lazyLoad
import net.kotlinx.kotest.BeSpecLight
import org.junit.jupiter.api.Test
import java.io.File

private const val temp = "sin"

class FileLazyLoadPropertyTest : BeSpecLight() {

    private val workspace = ResourceHolder.getWorkspace().slash("demo")

    private var fileS3: File by workspace lazyLoad "s3://$temp-work-dev/upload/iii/00000.txt.json"
    private var fileHttp: File by workspace.lazyLoad("http://xx/yy.txt")

    @Test
    fun test() {

        println(fileS3.length())
        println(fileS3.absolutePath)

        println(fileHttp.absolutePath)

    }

}