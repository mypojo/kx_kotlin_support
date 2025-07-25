package net.kotlinx.github

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 모든 API를 합친 클라이언트
 * retrofit2 를 사용함
 * */
interface GithubClient : GithubApiCommits {

    companion object {

        const val BASE_URL = "https://api.github.com/"

        /**
         * 아무 인증 없는 디폴트 클라이언트
         * 대상 저장소가 오픈소스인경우 이걸로도 작업 가능
         *  */
        val DEFAULT_CLIENT: GithubClient by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient.Builder().build())
                .build()
                .create(GithubClient::class.java)
        }

        /**
         * 토큰 인증을 사용하는 클라이언트
         * private 저장소에 접근 가능
         * @param token GitHub Personal Access Token 또는 GitHub App Token => repo 권한 다 주고 발급받으면 됨
         * */
        fun createWithToken(token: String): GithubClient {
            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()
                chain.proceed(authenticatedRequest)
            }

            val httpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()
                .create(GithubClient::class.java)
        }

        /**
         * GitHub App Installation Token을 사용하는 클라이언트
         * GitHub App을 통한 인증시 사용
         * @param installationToken GitHub App Installation Token
         * */
        fun createWithInstallationToken(installationToken: String): GithubClient {
            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "token $installationToken")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()
                chain.proceed(authenticatedRequest)
            }

            val httpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()
                .create(GithubClient::class.java)
        }
    }
}