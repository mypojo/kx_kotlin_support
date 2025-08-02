package net.kotlinx.dooray.drive

/**
 * 드라이브 아이템 (파일 또는 폴더)
 */
data class DoorayDriveFile(
    val id: String,
    val name: String,
    val version: Int,
    val createdAt: String,
    val updatedAt: String,
    val hasFolders: Boolean?,
    val mimeType: String,
    val size: Long,
    val annotations: Map<String, Any>?,
    val creator: User?,
    val lastUpdater: User?,
    val type: String, // "folder" or "file"
    val subType: String?,
) {

    /**
     * 사용자 정보
     */
    data class User(
        val id: String? = null,
        val name: String? = null,
    )

}