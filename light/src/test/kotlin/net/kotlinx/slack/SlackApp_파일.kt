package net.kotlinx.slack

import io.kotest.matchers.shouldBe
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.number.toLocalDateTime
import net.kotlinx.system.ResourceHolder
import net.kotlinx.time.toKr01

class SlackApp_파일 : BeSpecLight() {

    init {
        initTest(KotestUtil.SLOW)

        Given("SlackApp") {

            val app = koin<SlackApp>()
            val imgFile = ResourceHolder.getWorkspace().slash("test").slash("img.jpg")

            xThen("파일 업로드 (업로드 먼저 해야함)") {
                log.warn { "안되는데 이유몰랑. 일단 필요없어서 그냥 두기 -> 나중에 모듈화 하자." }
                imgFile.exists() shouldBe true
                val uploadV2 = app.methods().filesUploadV2 {
                    it.file(imgFile)
                    it.filename("imgFile_v2")
                    it.channel("#kx_alert")
                    it.title("xxx")
                }
                uploadV2.isOk shouldBe true
                log.debug { "uploadV2.file.urlPrivate ${uploadV2.file.urlPrivate}" }
            }

            Then("파일 리스트 조회") {
                val filesList = app.methods().filesList {
                    it.page(1)
                }
                filesList.files.forEach {
                    log.debug { "${it.id} : ${it.name} / ${it.urlPrivate} / ${(it.timestamp.toLong() * 1000).toLocalDateTime().toKr01()}" }
                }
            }

            xThen("업로드한 파일 전송") {
                val resp = app.methods().filesSharedPublicURL {
                    it.file("fileId")
                }
                resp.isOk shouldBe true
            }

        }
    }


}