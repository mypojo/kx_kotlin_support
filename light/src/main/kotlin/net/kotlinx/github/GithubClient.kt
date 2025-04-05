package net.kotlinx.github

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 모든 API를 합친 클라이언트
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
    }

}