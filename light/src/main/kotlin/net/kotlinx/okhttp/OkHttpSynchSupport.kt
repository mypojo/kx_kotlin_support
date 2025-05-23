package net.kotlinx.okhttp

import net.kotlinx.core.Kdsl
import net.kotlinx.csv.CsvReadWriteTool
import net.kotlinx.csv.CsvUtil
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream

//=======================================================================================================================================================
//==================================================== 경고!! 대량처리의 경우 asynch 하게 사용할것 ======================================================

/** 동기화 호출 (RMI) */
fun OkHttpClient.fetch(req: OkHttpReq): OkHttpResp = fetchInner(req).use { OkHttpResp(req, it).load() }

/**
 * 동기화 호출 후 body를 가져오지 않은 상태
 * stream을 직접 사용하려면 이렇게 사용해야함
 *  */
fun OkHttpClient.fetchInner(req: OkHttpReq): Response {
    this.reqInterceptor.invoke(req)
    return this.newCall(req.build()).execute()
}

/** 동기화 호출 (DSL) */
@Kdsl
fun OkHttpClient.fetch(block: OkHttpReq.() -> Unit): OkHttpResp = this.fetch(OkHttpReq().apply(block))

/**
 * 동기화 다운로드
 * 레거시 때문에 코드 수정하지 않음!!
 * 이거 쓰지말고 비동기 코드로 작업할것
 *  */
fun OkHttpClient.download(file: File, block: OkHttpReq.() -> Unit): OkHttpResp {
    val req = OkHttpReq().apply {
        mediaType = OkHttpMediaType.IMAGE  //기본 미디어타입 변경해줌
    }.apply(block)
    this.reqInterceptor.invoke(req)

    return this.newCall(req.build()).execute().use { response ->
        //파일 다운로드
        if (response.code == 200) {
            response.body.let {
                it!!.byteStream().apply {
                    file.outputStream().use<FileOutputStream, Unit> { fileOut ->
                        copyTo(fileOut, OkHttpUtil.BUFFER_SIZE)
                    }
                }
            }
        }
        OkHttpResp(req, response)
    }
}


/**
 * 업계에서 자주 쓰이는 짭퉁 TSV를 CSV로 변환해서 MS949로 다운로드 해준다
 * 일반적인 업무로직에서는 일단 원본 다운로드 후 처리하는방법을 권장.
 * 이 방법은 다운로드 시점에서 꼭 변환이 필요한 경우에 사용
 * */
suspend fun OkHttpClient.downloadTsvToCsv(url: String, file: File): OkHttpResp = this.download(url) {
    CsvReadWriteTool {
        readerInputStream = it
        readerFactory = { CsvUtil.TSV_UNOFFICIAL }
        writerFile = file
        writerFactory = { CsvUtil.ms949Writer() }
    }
}