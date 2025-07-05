package net.kotlinx.github

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

internal class GithubClientTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("깃헙") {

            When("퍼블릭 커밋") {
                val commits = GithubClient.DEFAULT_CLIENT.getCommits("mypojo", "kx_kotlin_support")
                commits.printSimple()
            }

            When("프라이빗 커밋") {
                val client = GithubClient.createWithToken("xx")
                val commits = client.getCommits("NHN-AD", "dmp")
                commits.printSimple()
            }

        }
    }
}