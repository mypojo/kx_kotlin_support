package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codecommit.listFileCommitHistory
import io.kotest.matchers.ints.shouldBeGreaterThan
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy


class CodeCommitSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("codeCommit") {

            Then("커밋 히스토리 가져오기") {

                //히스토리 가져오는게 없다.. 왜지??
                val history = aws.codeCommit.listFileCommitHistory {
                    this.repositoryName = findProfile97
                    this.maxResults = 2
                    this.commitSpecifier = "refs/heads/dev"
                    this.filePath = ".gitignore"
                }.revisionDag

                history.size shouldBeGreaterThan 1
            }

        }
    }

}