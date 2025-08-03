
package net.kotlinx.dooray.drive

import io.kotest.matchers.shouldNotBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class DoorayDriveDrivesTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        val client = DoorayDriveClient.create("fill your token")
        val driveId = "fill your driveId"
        val fileId = "fill your fileId"
        val parentId = "fill your parentId"

        Given("DoorayDriveDrives") {
            Then("drives - 개인 드라이브 목록 조회") {
                val actualResponse = client.drives()
                actualResponse.result shouldNotBe null
            }
        }

        Given("DoorayDriveFiles") {
            Then("getDriveItems - 특정 드라이브의 특정 폴더 내 아이템 목록 조회") {
                val actualResponse = client.getDriveItems(driveId, parentId)
                actualResponse.result shouldNotBe null
            }
        }

        Given("DoorayDriveFileDownload") {
            Then("downloadFile - 파일 다운로드 URL 조회") {
                val actualResponse = client.downloadFile(driveId, fileId)
                actualResponse.headers()["location"] shouldNotBe null
            }
        }

        Given("DoorayDriveFileUpload & DoorayDriveFileUpdate & DoorayDriveFileDelete") {

            val tempFile = File("temp.txt").apply {
                writeText("temporary file for testing")
                deleteOnExit()
            }
            val requestFile = tempFile.asRequestBody("text/plain".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

            Then("uploadFile and updateFile - 파일 업로드 및 수정") {
                // 1. 파일 업로드
                val uploadResponse = client.uploadFile(driveId, parentId, body)
                uploadResponse.body()?.result?.id shouldNotBe null
                val uploadedFileId = uploadResponse.body()!!.result!!.id

                // 2. 파일 수정
                val updatedFile = File("updated_temp.txt").apply {
                    writeText("updated file content")
                    deleteOnExit()
                }
                val updatedRequestFile = updatedFile.asRequestBody("text/plain".toMediaTypeOrNull())
                val updatedBody = MultipartBody.Part.createFormData("file", updatedFile.name, updatedRequestFile)
                val updateResponse = client.updateFile(driveId, uploadedFileId, updatedBody)
                updateResponse.body()?.result?.id shouldNotBe null
            }
        }
    }
}
