package net.kotlinx.dooray.drive

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 특정 드라이브의 특정 폴더 내 아이템 목록 조회
 */
interface DoorayDriveFiles {
    @GET("drive/v1/drives/{drive-id}/files")
    suspend fun getDriveItems(
        @Path("drive-id") driveId: String,
        @Query("parentId") parentId: String = "root",
        @Query("type") type: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): DoorayDriveResponse<List<DoorayDriveFile>>


}
