package net.kotlinx.sftp

/** 걍 설정 저장용  */
data class SftpConfig(
    val name: String,
    val ip: String,
    val id: String,
    var pwd: String,
    val port: Int = 22,
)