package net.kotlinx.okhttp

import net.kotlinx.core.string.toTextGrid
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class HttpDomainConverterTest : TestRoot() {

    @Test
    fun test() {

        val converter0 = HttpDomainConverter {}
        val converter1 = HttpDomainConverter {
            pathLevel = 1
            toKr = false
        }
        val converter100 = HttpDomainConverter {
            pathLevel = 100
            toKr = false
        }

        val datas = listOf(
            "http://homepage-mob.skcarrental.com/a/b?a=b",
            "http://homepage-mob.skcarrental.com/a/b/?a=b",
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
        listOf("url", "정규화1", "정규화2","정규화100").toTextGrid(datas).print()


    }


}