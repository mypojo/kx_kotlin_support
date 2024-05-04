package net.kotlinx.okhttp

import net.kotlinx.core.string.toTextGrid
import net.kotlinx.kotest.BeSpecLog
import org.junit.jupiter.api.Test

class HttpDomainUtilTest : BeSpecLog() {
    init {

        @Test
        fun test() {
            val datas = listOf(
                "http://homepage-mob.skcarrental.com/a/b?a=b",
                "http://homepage-mob.skcarrental.com/a/b/?a=b",
                "https://homepage-mob.skcarrental.com/",
                "http://aa.homepage-mob.skcarrental.com/aaa/",
                "https://brand.naver.com/aaa",
            ).map {
                arrayOf(it, HttpDomainUtil.normalize(it), HttpDomainUtil.normalize(it, 1))
            }
            listOf("url", "정규화", "정규화L2").toTextGrid(datas).print()


        }
    }
}