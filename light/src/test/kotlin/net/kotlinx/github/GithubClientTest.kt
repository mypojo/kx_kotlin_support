package net.kotlinx.github

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

internal class GithubClientTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("깃헙") {
            Then("기본 커밋 리스트 불러오기") {
                val commits = GithubClient.DEFAULT_CLIENT.getCommits("mypojo", "kx_kotlin_support")
                commits.printSimple()
            }
        }
    }
}