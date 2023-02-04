package net.kotlinx.http

import mu.KotlinLogging
import okhttp3.*
import okio.IOException
import org.junit.jupiter.api.Test

internal class OkHttpSupportTest {

    private val log = KotlinLogging.logger {}

    private val client = OkHttpClient()

    @Test
    fun `기본테스트`() {

        val request = Request.Builder().url("https://publicobject.com/helloworld.txt").build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            for ((name, value) in response.headers) {
                println("$name: $value")
            }
            println("msg : ${response.message}")
            println(response.code)
            println(response.body!!.string())
        }

    }

    fun `비동기`() {
        val request = Request.Builder()
            .url("http://publicobject.com/helloworld.txt")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }

                    println(response.body!!.string())
                }
            }
        })
    }

}