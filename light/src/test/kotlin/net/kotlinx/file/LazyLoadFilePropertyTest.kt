package net.kotlinx.file

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThan
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.lazyLoad.lazyLoad
import net.kotlinx.number.toSiText
import net.kotlinx.reflect.name
import net.kotlinx.system.ResourceHolder
import java.io.File


class LazyLoadFilePropertyTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.PROJECT)

        Given("LazyLoadFileProperty") {

            val workspace = ResourceHolder.WORKSPACE.slash(LazyLoadFilePropertyTest::class.name())

            When("간단한 설정으로 외부 리소스를 로컬 File로 가져온다") {

                xThen("S3 파일 늦은로드 (프로파일 입력버전) - 프로파일 수정필요") {

                    val profileName by lazy { findProfile28 }
                    val localFile = workspace.slash("s3.json")
                    localFile.delete()

                    val filePath = "s3://cdk-hnb659fds-assets-xxx-ap-northeast-2/eee.json"
                    val file: File by localFile lazyLoad {
                        info = filePath
                        profile = profileName
                    }
                    file.length() shouldBeGreaterThan 1000
                    log.info { "fileS3 ${file.absolutePath} : ${file.length().toSiText()}" }
                }


                Then("S3 디렉토리 늦은로드 (프로파일 입력버전)") {

                    val profileName by lazy { findProfile97 }
                    val localFile = workspace.slash("fonts")
                    //localFile.deleteRecursively()

                    val filePath = "s3://${findProfile97}-data-dev/resource/font/"
                    val file: File by localFile lazyLoad {
                        info = filePath
                        profile = profileName
                    }
                    file.listFiles().size shouldBeGreaterThan  1
                }

                Then("http 파일 늦은로드 (단축 버전)") {
                    val file: File by workspace.slash("naver.html") lazyLoad "https://www.naver.com/"
                    file.length() shouldBeGreaterThan 1000
                    log.info { "fileS3 ${file.absolutePath} : ${file.length().toSiText()}" }
                }
            }
        }
    }


}