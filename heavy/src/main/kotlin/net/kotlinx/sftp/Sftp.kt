package net.kotlinx.sftp

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
import mu.KotlinLogging
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * JSch 를 그냥 쓰기에는 고통이 심해서 만들었음. 풀 같은거 없고 그냥 즉시 만들어 쓰고 닫을것
 *
 * 경로 붙일때 마지막에 "/"  이거 넣지 말것!
 * 데이터가 너무 많을경우 sftp.cd(dir); 이후 antPAth로 검색도 가능하다.
 */
@Suppress("UNCHECKED_CAST")
class Sftp(
    private val sftpConfig: SftpConfig,
    private val config: Properties = Properties(),
) : Closeable {

    init {
        config["StrictHostKeyChecking"] = "no"
    }

    private val jsch by lazy { JSch() }
    private val session: Session by lazy {
        jsch.getSession(sftpConfig.id, sftpConfig.ip, sftpConfig.port).apply {
            setPassword(sftpConfig.pwd)
            setConfig(config)
            connect()
        }
    }
    private val channel: ChannelSftp by lazy {
        session.openChannel("sftp").apply {
            connect()
        } as ChannelSftp
    }

    override fun close() {
        channel.disconnect()
        session.disconnect()
    }

    private val log = KotlinLogging.logger {}

    /**
     * 해당 경로 이후의 모든 파일을 검색해서 flat 리스트로 리턴한다.
     * 일단 다 가져온 후 인메모리에서 필터링 한다. (문제시 수정)
     * 풀 패스가 리턴된다. ex)  /nas/eap/backup/20170330/conv_RI003_COLLECT01.on.2017-03-30.zip
     */
    fun flatFiles(dir: String): List<SftpRemoteFile> {
        val files: MutableList<SftpRemoteFile> = Lists.newArrayList()
        listFiles(files, dir)
        return files
    }

    fun ls(dir: String, isDir: Boolean): List<String> {
        val filelist: List<ChannelSftp.LsEntry> = channel.ls(dir) as List<ChannelSftp.LsEntry>
        log.trace("{}개의 파일 검색 from {}", filelist.size, dir)
        val paths: MutableList<String> = Lists.newArrayList()
        for (entry in filelist) {
            val attrs = entry.attrs
            if (attrs.isLink) continue  //링크가 뭔지 몰라도 스킵
            if (attrs.isDir != isDir) continue
            val name = entry.filename
            if (name.startsWith(".")) continue  //  .으로 시작하는애들 무시
            val newPath = "$dir/$name"
            paths.add(newPath)
        }
        return paths
    }

    private fun listFiles(result: MutableList<SftpRemoteFile>, dir: String) {
        val filelist: List<ChannelSftp.LsEntry> = channel.ls(dir) as List<ChannelSftp.LsEntry>
        log.trace("{}개의 파일 검색 from {}", filelist.size, dir)
        for (entry in filelist) {
            val attrs = entry.attrs
            if (attrs.isLink) continue  //링크가 뭔지 몰라도 스킵
            val name = entry.filename
            val newPath = "$dir/$name"
            if (attrs.isDir) {
                if (name.startsWith(".")) continue  //  .으로 시작하는애들 무시
                listFiles(result, newPath)
            } else {
                val file = SftpRemoteFile(newPath, attrs.size)
                result.add(file)
            }
        }
    }

    /**
     * 스래드 안전하지 않더라.
     */
    @Synchronized
    fun download(remoteFile: String?, downloadFile: File) {
        downloadFile.parentFile.mkdirs()
        //SftpATTRS attrs =  sftp.lstat(remoteFile);
        channel[remoteFile, downloadFile.absolutePath]
        log.trace("파일 다운로드 완료.  {} => {}", remoteFile, downloadFile.absolutePath)
    }

    /**
     * 디렉토리 전체를 다운로드
     */
    fun downloadDir(remoteDir: String, downloadDir: File?) {
        val remoteFiles = flatFiles(remoteDir)
        for ((file) in remoteFiles) {
            val fileName: String = file.substringAfterLast("/")
            download(file, File(downloadDir, fileName))
        }
    }

    private fun isDir(path: String): Boolean {
        return try {
            val attrs = channel.lstat(path)
            attrs.isDir
        } catch (e: SftpException) {
            if (e.id == 2) return false //2: No such file
            throw e
        }
    }

    /**
     * 한뎁스씩만 만들 수 있다.
     */
    private fun mkParentDir(remoteFile: String) {
        val parent = remoteFile.substringAfterLast("/") //이거 확인
        if (Strings.isNullOrEmpty(parent)) return  //루트일경우 무시
        if (isDir(parent)) return
        log.info { "원격지에 디렉토리를 생성합니다.  $parent" }
        try {
            channel.mkdir(parent)
        } catch (e: SftpException) {
            if (e.id == 2) { //부모도 없을경우
                mkParentDir(parent)
                channel.mkdir(parent)
            } else {
                throw e
            }
        }
    }

    /**
     * 간단 업로드. 디렉토리가 없으면 생성해준다
     */
    @Synchronized
    fun upload(localFile: File, remoteFile: String) {
        mkParentDir(remoteFile)
        //확인필요.
        FileInputStream(localFile).use {
            channel.put(it, remoteFile, null, ChannelSftp.OVERWRITE)
            log.trace("파일 업로드 완료.  {} => {}", localFile.absolutePath, remoteFile)
        }
    }

    /**
     * 파일 존재 여부
     */
    fun exists(path: String): Boolean {
        lateinit var res: Vector<*>
        try {
            res = channel.ls(path)
        } catch (e: SftpException) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false
            }
            throw e
        }
        return res.isEmpty()
    }

}
