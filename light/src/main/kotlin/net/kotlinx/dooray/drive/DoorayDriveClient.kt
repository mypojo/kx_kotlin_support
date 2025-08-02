package net.kotlinx.dooray.drive

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Dooray Drive API 클라이언트
 * retrofit2 를 사용함
 */
interface DoorayDriveClient : DoorayDriveDrives, DoorayDriveFileDownload, DoorayDriveFiles, DoorayDriveFileUpdate, DoorayDriveFileUpload {

    companion object {

        const val BASE_URL = "https://api.dooray.com/" // 실제 Dooray API URL로 변경해야 합니다.

        /**
         * 토큰 인증을 사용하는 클라이언트
         * @param token Dooray API Token
         */
        fun create(token: String): DoorayDriveClient {
            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "dooray-api $token") // 실제 인증 헤더 형식으로 변경해야 합니다.
                    .build()
                chain.proceed(authenticatedRequest)
            }

            val httpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .followRedirects(false) // 307 Redirect를 직접 처리하기 위해 비활성화
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()
                .create(DoorayDriveClient::class.java)
        }
    }
}