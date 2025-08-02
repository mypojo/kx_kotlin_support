package net.kotlinx.dooray.drive

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * 파일 수정 (307 Redirect 발생)
 */
interface DoorayDriveFileUpdate {
    @Multipart
    @PUT("drive/v1/drives/{driveId}/files/{fileId}?media=raw")
    suspend fun updateFile(
        @Path("driveId") driveId: String,
        @Path("fileId") fileId: String,
        @Part file: MultipartBody.Part
    ): Response<DoorayDriveResponse<DoorayDriveFile>>
}
