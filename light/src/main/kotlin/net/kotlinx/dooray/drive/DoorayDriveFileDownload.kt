package net.kotlinx.dooray.drive

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 파일 다운로드 URL 조회 (307 Redirect 발생)
 */
interface DoorayDriveFileDownload {

    @GET("drive/v1/drives/{drive-id}/files/{file-id}?media=raw")
    suspend fun downloadFile(@Path("drive-id") driveId: String, @Path("file-id") fileId: String): Response<Void>
}
