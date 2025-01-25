package net.kotlinx.file

import io.kotest.engine.spec.tempdir
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class FileGzipUtilTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)

        Given("FileGzipUtil") {

            val text = "안녕하세요\n영감님?" + "안녕하세요\n영감님?" + "안녕하세요\n영감님?"
            val temp = tempdir()
            //val temp = ResourceHolder.WORKSPACE
            val file = temp.slash("demo.txt")
            file.writeText(text)

            Then("압축하기") {
                val gzip = file.gzip()
                log.debug { " ${file.length()} -> ${gzip.length()}" }
                gzip.length() shouldBeGreaterThan 10
            }

            Then("압축하고 풀기") {
                val gzip = file.gzip()
                log.debug { " ${file.length()} -> ${gzip.length()}" }

                check(file.delete())

                val unGzip = gzip.unGzip()
                println(unGzip.readText())
                unGzip.readText() shouldBe text
            }


        }
    }

}
