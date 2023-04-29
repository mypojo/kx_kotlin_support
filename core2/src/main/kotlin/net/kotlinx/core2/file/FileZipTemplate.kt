package net.kotlinx.core2.file

import mu.KotlinLogging
import net.kotlinx.core1.collection.endsWithAny
import java.io.File

/**
 * 범용적인 압축 도구.
 * 1. 특정 용량을 검기면 압축해줌
 * 2. 파일이 여러개면 압축해줌 (다운로드 편리성 때문)
 * */
class FileZipTemplate(
    /** 이 크기를 넘어가면 압축함 . 기본 100mb */
    private val zipLimitMb: Int = 100
) {

    private val log = KotlinLogging.logger {}

    /** 간단 압축 */
    fun zip(orgFile: File): File = when {
        orgFile.isFile -> zipSingleFile(orgFile)
        orgFile.listFiles().size == 1 -> {
            val result = zipSingleFile(orgFile.listFiles().first())
            File(result.parentFile.parentFile, "${orgFile.name}.${result.name.substringAfterLast(".")}").apply {
                result.renameTo(this)
                if (orgFile.isDirectory) orgFile.delete() //남은거 지워준다.
            }
        }

        else -> zipDir(orgFile)
    }

    private fun zipDir(dir: File): File {
        check(dir.isDirectory)
        log.info { "파일 수  [${dir.listFiles().size}]개 -> 압축합니다." }
        val zipFile = FileZipUtil.zipDirectory(dir)
        check(dir.deleteRecursively()) { "파일삭제 실패" }
        return File(zipFile.parentFile, "${dir.name}.zip").also {
            zipFile.renameTo(it)
        }
    }

    private fun zipSingleFile(orgFile: File): File {
        val mb: Long = orgFile.length() / 1024 / 1024
        return when {
            FileZipUtil.ZIP_EXT.endsWithAny(orgFile.name) -> orgFile //이미 압축된건
            mb >= zipLimitMb -> {
                log.info { "파일 용량 ${mb}mb -> 제한(${zipLimitMb}mb) 초과 -> 압축합니다." }
                File(orgFile.parentFile, orgFile.name + ".zip").also {
                    FileZipUtil.zip(it, orgFile)
                    orgFile.delete()
                }
            }

            else -> orgFile
        }
    }


}