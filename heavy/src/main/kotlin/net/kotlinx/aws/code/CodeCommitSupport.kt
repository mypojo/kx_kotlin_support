package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codecommit.CodeCommitClient
import aws.sdk.kotlin.services.codecommit.getBranch
import aws.sdk.kotlin.services.codecommit.getCommit
import aws.sdk.kotlin.services.codecommit.model.Commit
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.codeCommit: CodeCommitClient
    get() = getOrCreateClient { CodeCommitClient { awsConfig.build(this) }.regist(awsConfig) }

/** 간단 샘플 */
suspend fun CodeCommitClient.getCommit(repositoryName: String, commitId: String): Commit {
    return this.getCommit {
        this.commitId = commitId
        this.repositoryName = repositoryName
    }.commit!!
}

/** 특정 브랜치의 가장최근 커밋 */
suspend fun CodeCommitClient.getBranchCommit(repositoryName: String, branchName: String): Commit {
    val branch = this.getBranch {
        this.repositoryName = repositoryName
        this.branchName = branchName
    }
    val resp = this.getCommit {
        this.repositoryName = repositoryName
        this.commitId = branch.branch!!.commitId!!
    }
    return resp.commit!!
}