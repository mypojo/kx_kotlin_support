package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codecommit.listFileCommitHistory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy


class CodeCommitSupportKtTest : BeSpecHeavy() {

    private val profileName by lazy { findProfile28() }
    private val aws by lazy { koin<AwsClient>(profileName) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("codeCommit") {

            Then("커밋 히스토리 가져오기") {

                //히스토리 가져오는게 없다.. 왜지??
                val history = aws.codeCommit.listFileCommitHistory {
                    this.repositoryName = profileName
                    this.maxResults = 2
                    this.commitSpecifier = "refs/heads/dev"
                    this.filePath = ".gitignore"
                }.revisionDag

                history[0].commit!!.committer!!.name shouldBe contain("sin")
            }

        }
    }

}