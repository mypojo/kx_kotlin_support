package net.kotlinx.dooray.drive

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * 파일 업로드 (307 Redirect 발생)
 */
interface DoorayDriveFileUpload {
    @Multipart
    @POST("drive/v1/drives/{driveId}/files")
    suspend fun uploadFile(
        @Path("driveId") driveId: String,
        @Query("parentId") parentId: String,
        @Part file: MultipartBody.Part
    ): Response<DoorayDriveResponse<DoorayDriveFile>>
}
