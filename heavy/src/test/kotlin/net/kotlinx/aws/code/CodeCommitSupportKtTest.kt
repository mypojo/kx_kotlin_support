package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codecommit.getDifferences
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins
import net.kotlinx.test.MyHeavyKoinStarter
import net.kotlinx.test.TestHeavy
import org.junit.jupiter.api.Test


class CodeCommitSupportKtTest : TestHeavy() {

    companion object {
        private const val PROJECT = "xx"
        init {
            MyHeavyKoinStarter.startup(PROJECT)
        }
    }

    @Test
    fun getCommit() {

        val aws = Koins.get<AwsClient>()

        runBlocking {

            val commit = aws.codeCommit.getCommit(PROJECT, "89ce3505dff8e3cf25657f208974c86b82888cee")
            println(commit)


            val paginated = aws.codeCommit.getDifferences {
                this.repositoryName = PROJECT
                this.afterCommitSpecifier = "89ce3505dff8e3cf25657f208974c86b82888cee"
                this.beforeCommitSpecifier = "7ea15ce263841d035524ed1a14a73e89041996bc"
            }
            println(paginated.differences!!.size)

        }



    }
}