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

        val datas = listOf(
            "http://homepage-mob.skcarrental.com/a/b?a=b",
            "http://homepage-mob.skcarrental.com/a/b/?a=b",
            "https://homepage-MOB.skcarrental.com/",
            "http://aa.homepage-mob.skcarrental.com/aaa/",
            "https://brand.naver.com/aaa",
            "http://집밥.com/test",
        ).map {
            arrayOf(it, converter0.normalize(it), converter1.normalize(it))
        }
        listOf("url", "정규화1", "정규화2").toTextGrid(datas).print()


    }


}