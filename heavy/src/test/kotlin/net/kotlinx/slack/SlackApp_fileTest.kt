package net.kotlinx.slack

import com.slack.api.methods.request.files.FilesSharedPublicURLRequest
import net.kotlinx.kotest.BeSpecLight
import org.junit.jupiter.api.Test
import java.io.File


private const val userToken = "xoxp-xxxx"

class SlackApp_fileTest : BeSpecLight() {

    //val token = get<AwsClient1>().ssmStore["/slack/token"]!!
    val app = SlackApp(userToken)

    val imgFile = File("D:\\DATA\\WORK\\ad2.png")


    @Test
    fun `이미지업로드`() {
//        val req = FilesUploadV2Request.builder().file(imgFile).filename("v1").channel("#kx_alert").title("xxx").build()
//        val uploadV2 = app.slack.methods(userToken).filesUploadV2(req)
//        println(uploadV2.file.urlPrivate) //https://files.slack.com/files-pri/T0648ESH4BD-F06AY1Y5UR0/ad2.png
//        println(uploadV2.file.id) //https://files.slack.com/files-pri/T0648ESH4BD-F06AY1Y5UR0/ad2.png
//        println(uploadV2.file.name)
//        val filesList = app.slack.methods(userToken).filesList {
//            it.page(1)
//        }
//        filesList.files.forEach {
//            println("${it.id} : ${it.name} / ${it.urlPrivate} / ${(it.timestamp.toLong()*1000).toLocalDateTime().toKr01()}")
//        }
//
        println(filesSharedPublicURL("F06BANNB541"))

    }

    fun filesSharedPublicURL(fileId: String): com.slack.api.model.File {
        val req = FilesSharedPublicURLRequest.builder().file(fileId).token(userToken).build()
        val resp = app.slack.methods(userToken).filesSharedPublicURL(req)!!
        if (!resp.isOk) {
            throw IllegalStateException("파일 [${fileId}] filesSharedPublicURL 에러 : ${resp.error}")
        }
        return resp.file!!
    }

}