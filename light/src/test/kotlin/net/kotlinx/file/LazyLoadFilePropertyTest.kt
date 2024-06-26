package net.kotlinx.file

import io.kotest.matchers.longs.shouldBeGreaterThan
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.lazyLoad.LazyLoadFileProperty
import net.kotlinx.lazyLoad.lazyLoad
import net.kotlinx.number.toSiText
import net.kotlinx.reflect.name
import net.kotlinx.system.ResourceHolder
import java.io.File


class LazyLoadFilePropertyTest : BeSpecHeavy() {

    private val profileName by lazy { findProfile28() }

    init {
        initTest(KotestUtil.PROJECT)

        Given("LazyLoadFileProperty") {

            val workspace = ResourceHolder.getWorkspace().slash(LazyLoadFilePropertyTest::class.name())

            When("간단한 설정으로 외부 리소스를 로컬 File로 가져온다") {

                Then("S3 파일 늦은로드") {
                    val localFile = workspace.slash("s3.json")
                    localFile.delete()

                    //val file: File by localFile lazyLoad "s3://cdk-hnb659fds-assets-289023186990-ap-northeast-2/0230f786c817c740269bf7f9a5149f41435f3298b8f3f73ab3dd6664c5c94ef2.json"
                    val file: File by LazyLoadFileProperty(
                        localFile,
                        "s3://cdk-hnb659fds-assets-289023186990-ap-northeast-2/0230f786c817c740269bf7f9a5149f41435f3298b8f3f73ab3dd6664c5c94ef2.json",
                        profileName
                    )
                    file.length() shouldBeGreaterThan 1000
                    log.info { "fileS3 ${file.absolutePath} : ${file.length().toSiText()}" }
                }

                Then("http 파일 늦은로드") {
                    val file: File by workspace.slash("naver.html").lazyLoad("https://www.naver.com/")
                    file.length() shouldBeGreaterThan 1000
                    log.info { "fileS3 ${file.absolutePath} : ${file.length().toSiText()}" }
                }
            }
        }
    }


}