package net.kotlinx.aws.okhttp

import okhttp3.OkHttpClient
import java.io.File
import java.io.FileOutputStream


/** 동기화 호출 (RMI) */
fun OkHttpClient.fetch(okHttpReq: OkHttpReq): OkHttpResp = this.newCall(okHttpReq.build()).execute().use { OkHttpResp(okHttpReq, it).load() }

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
