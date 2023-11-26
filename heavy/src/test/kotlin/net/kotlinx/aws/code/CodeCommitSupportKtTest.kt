package net.kotlinx.aws.code

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


            //히스토리 가져오는게 없다.. 왜지??
//            val history = aws.codeCommit.listFileCommitHistory {
//                this.repositoryName = PROJECT
//                this.maxResults = 2
//                this.commitSpecifier = "refs/heads/dev"
//                this.filePath = ".gitignore"
//            }.revisionDag


//            println(history[0].commit!!.commitId)
//            println(history[0].commit!!.committer)

//            val paginated = aws.codeCommit.getDifferences {
//                this.repositoryName = PROJECT
//                this.afterCommitSpecifier = "89ce3505dff8e3cf25657f208974c86b82888cee"
//                this.beforeCommitSpecifier = "7ea15ce263841d035524ed1a14a73e89041996bc"
//            }
//
//            log.warn { "== 멀티프로젝트 체크 ===" }
//
//            paginated.differences!!.forEach {
////                val file = aws.codeCommit.getFile {
////                    this.repositoryName = PROJECT
////                    this.filePath = it.afterBlob!!.path
////                }
////                println(String((file.fileContent)))
//                println(it.afterBlob!!.path)
//            }

        }


    }
}