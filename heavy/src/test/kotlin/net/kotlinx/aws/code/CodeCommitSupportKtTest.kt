package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codecommit.listFileCommitHistory
import io.kotest.matchers.ints.shouldBeGreaterThan
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print


class CodeCommitSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("codeCommit") {

            Then("특정 파일의 커밋 히스토리 가져오기") {
                val history = aws.codeCommit.listFileCommitHistory {
                    this.repositoryName = findProfile97
                    this.maxResults = 2
                    this.commitSpecifier = "refs/heads/dev"
                    this.filePath = ".gitignore"
                }.revisionDag

                history.size shouldBeGreaterThan 1
                history.print()
            }

            Then("특정 브랜치의 최근 히스토리 가져오기") {
                val commit = aws.codeCommit.getBranchCommit(findProfile97, "dev")
                listOf(commit).print()
            }

        }
    }

}