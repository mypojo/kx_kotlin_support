package net.kotlinx.okhttp

import io.kotest.matchers.shouldNotBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.toTextGrid

class HttpDomainConverterTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("HttpDomainConverter") {

            val converter0 = HttpDomainConverter {}
            val converter1 = HttpDomainConverter {
                pathLevel = 1
                toKr = false
            }
            val converter100 = HttpDomainConverter {
                pathLevel = 10
                toKr = false
            }

            Then("도메인을 적절하게 정규화") {
                val datas = listOf(
                    "http://homepage-mob.skcarrental.com/a/b?a=b",
                    "http://homepage-mob.skcarrental.com/a/B/?a=b",
                    "https://homepage-MOB.skcarrental.com/",
                    "http://aa.homepage-mob.skcarrental.com/aaa/",
                    "https://brand.naver.com/aaa",
                    "https://brand.naver.com/aaa/bb/CC/dd",
                    "http://집밥.com/test",
                    "brand.naver.com/SM5",
                ).map {
                    arrayOf(
                        it,
                        converter0.normalize(it),
                        converter1.normalize(it),
                        converter100.normalize(it),
                    )
                }
                listOf("url", "정규화1", "정규화2", "정규화100").toTextGrid(datas).print()

                datas[0][0] shouldNotBe "homepage-mob.skcarrental.com"
                datas[0][2] shouldNotBe "homepage-mob.skcarrental.com/a/b"
            }
        }
    }
}