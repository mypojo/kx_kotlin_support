package net.kotlinx.okhttp

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class HttpDomainUtilTest : TestRoot(){

    @Test
    fun test() {

        println(HttpDomainUtil.normalize("http://homepage-mob.skcarrental.com/a=b"))
        println(HttpDomainUtil.normalize("https://homepage-mob.skcarrental.com/"))
        println(HttpDomainUtil.normalize("aa.homepage-mob.skcarrental.com//"))

    }

}