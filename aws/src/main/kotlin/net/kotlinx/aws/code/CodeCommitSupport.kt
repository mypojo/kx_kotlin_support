package net.kotlinx.aws.code

import aws.sdk.kotlin.services.codecommit.CodeCommitClient
import aws.sdk.kotlin.services.codecommit.getCommit
import aws.sdk.kotlin.services.codecommit.model.Commit


/** 간단 샘플 */
suspend fun CodeCommitClient.getCommit(repositoryName: String, commitId: String): Commit {
    return this.getCommit {
        this.commitId = commitId
        this.repositoryName = repositoryName
    }.commit!!
}