package net.kotlinx.okhttp

import net.kotlinx.core.Kdsl
import okhttp3.OkHttpClient
import java.io.File
import java.io.FileOutputStream

//=======================================================================================================================================================
//==================================================== 경고!! 대량처리의 경우 asynch 하게 사용할것 ======================================================

/** 동기화 호출 (RMI) */
fun OkHttpClient.fetch(req: OkHttpReq): OkHttpResp{
    this.reqInterceptor.invoke(req)
    return this.newCall(req.build()).execute().use { OkHttpResp(req, it).load() }
}

/** 동기화 호출 (DSL) */
@Kdsl
fun OkHttpClient.fetch(block: OkHttpReq.() -> Unit): OkHttpResp = this.fetch(OkHttpReq().apply(block))

/** 동기화 다운로드 */
fun OkHttpClient.download(file: File, block: OkHttpReq.() -> Unit): OkHttpResp {
    val req = OkHttpReq().apply {
        mediaType = OkHttpMediaType.IMAGE  //기본 미디어타입 변경해줌
    }.apply(block)
    this.reqInterceptor.invoke(req)

    return this.newCall(req.build()).execute().use { response ->
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
        OkHttpResp(req, response)
    }
}
