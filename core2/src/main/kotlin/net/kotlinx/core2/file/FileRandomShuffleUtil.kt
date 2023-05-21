package net.kotlinx.core2.file

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * window에서 zip 이미지 볼때 셔플 시켜줌
 * 1뎁스만 지원
 *  */
class FileRandomShuffleUtil(
    private val uuidSeparator: String = "#",
    private val pathSeparator: String = "@",
    private val exts: Set<String> = setOf("rar", "zip"),
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /** 파일들을 디렉토리 경로를 보관하면서 펼쳐준다. */
    fun flat(workDir: String, toPath: String) {

        val outDir = File(toPath)
        outDir.mkdirs()
        val outDirLength = outDir.absolutePath.length -1

        Files.walk(Paths.get(workDir))
            .map { it.toFile() }
            .filter { it.isFile }
            .filter { exts.any { ext -> it.name.endsWith(ext) } }
            .forEach {
                val endIndex = it.absolutePath.length - it.name.length -1
                val midPath = it.absolutePath.substring(outDirLength, endIndex)
                val newFile = File(outDir, "$midPath$pathSeparator${it.name}")
                it.renameTo(newFile)
                log.trace("move {} => {}", it.absolutePath ,newFile.absolutePath)
            }

    }

    /** 그자리에서 접미어에 uuid를 붙여서 셔플 */
    fun shuffle(workDir: String) {
        Files.walk(Paths.get(workDir))
            .map { it.toFile() }
            .filter { it.isFile }
            .filter { exts.any { ext -> it.name.endsWith(ext) } }
            .filter { !it.name.contains(uuidSeparator) }
            .forEach {
                val uuid = UUID.randomUUID().toString()
                val newFile = File(workDir, "$uuid$uuidSeparator${it.name}")
                it.renameTo(newFile)
                log.trace("shuffle {} => {}", it, newFile)
            }
    }

    /** 셔플한거 풀어줌 */
    fun unshuffle(workDir: String) {

        Files.walk(Paths.get(workDir))
            .map { it.toFile() }
            .filter { it.isFile }
            .filter { exts.any { ext -> it.name.endsWith(ext) } }
            .filter { it.name.contains(uuidSeparator) }
            .forEach {
                val renamed = it.name.substringAfter(uuidSeparator)
                val newFile = File(workDir, renamed)
                it.renameTo(newFile)
                log.trace("unshuffle {} => {}", it, newFile)
            }
    }
}
