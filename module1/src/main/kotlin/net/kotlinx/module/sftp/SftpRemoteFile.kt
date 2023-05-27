package net.kotlinx.module.sftp

import java.io.File

data class SftpRemoteFile(
    val file: String,
    var size: Long = 0,
    var localDownload: File? = null
)
