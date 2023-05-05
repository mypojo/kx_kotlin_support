package net.kotlinx.module1.okhttp

import okhttp3.OkHttpClient
import okio.GzipSource
import okio.buffer
import java.io.File
import java.io.FileOutputStream


/** 동기화 호출 (RMI) */
fun OkHttpClient.fetch(okHttpReq: OkHttpReq): OkHttpResp = this.newCall(okHttpReq.build()).execute().use {
    //gzip이면 풀어준다
    println("$$$")
    val text = if (it.header("Content-Encoding") == "gzip") {
        GzipSource(it.body.source()).buffer().readUtf8()
    } else {
        it.body.string()
    }
    OkHttpResp(okHttpReq, it, text)
}

/** 동기화 호출 (DSL) */
fun OkHttpClient.fetch(block: OkHttpReq.() -> Unit): OkHttpResp = this.fetch(OkHttpReq().apply(block))

/** 동기화 다운로드 */
fun OkHttpClient.download(file: File, block: OkHttpReq.() -> Unit): OkHttpResp {
    val okHttpReq = OkHttpReq().apply {
        mediaType = OkHttpUtil.MEDIA_TYPE_IMAGE  //기본 미디어타입 변경해줌
    }.apply(block)
    return this.newCall(okHttpReq.build()).execute().use { response ->
        //파일 다운로드
        if (response.code == 200) {
            response.body.let {
                it.byteStream().apply {
                    file.outputStream().use<FileOutputStream, Unit> { fileOut ->
                        copyTo(fileOut, OkHttpUtil.BUFFER_SIZE)
                    }
                }
            }
        }
        OkHttpResp(okHttpReq, response)
    }
}
