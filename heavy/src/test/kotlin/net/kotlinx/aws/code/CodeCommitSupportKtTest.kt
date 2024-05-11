package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codecommit.listFileCommitHistory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy


class CodeCommitSupportKtTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.PROJECT02)

        Given("codeCommit") {

            val aws = Koins.koin<AwsClient>()
            val awsConfig = aws.awsConfig

            Then("커밋 히스토리 가져오기") {

                //히스토리 가져오는게 없다.. 왜지??
                val history = aws.codeCommit.listFileCommitHistory {
                    this.repositoryName = awsConfig.profileName
                    this.maxResults = 2
                    this.commitSpecifier = "refs/heads/dev"
                    this.filePath = ".gitignore"
                }.revisionDag

                history[0].commit!!.committer!!.name shouldBe contain("sin")
            }

        }
    }

}