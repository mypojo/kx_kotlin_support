package net.kotlinx.dooray.drive

import retrofit2.http.GET

/**
 * 사용자 개인 드라이브 API
 */
interface DoorayDriveDrives {
    /**
     * 사용자 개인 드라이브 목록 조회
     */
    @GET("drive/v1/drives?type=private")
    suspend fun drives(): DoorayDriveResponse<List<DoorayDrive>>

    /**
     * 드라이브 정보
     */
    data class DoorayDrive(
        val id: String,
        val project: DoorayProject?,
        val name: String,
        val type: String,
    )

    /**
     * 드라이브에 포함된 프로젝트 정보
     */
    data class DoorayProject(
        val id: String?,
    )

}



