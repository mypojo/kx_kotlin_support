package net.kotlinx.aws.codeCommit

import aws.sdk.kotlin.services.codecommit.CodeCommitClient
import aws.sdk.kotlin.services.codecommit.getBranch
import aws.sdk.kotlin.services.codecommit.getCommit
import aws.sdk.kotlin.services.codecommit.model.Commit
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist


/**
 * codecommit 은 이제 지원이 중단도미.. by 2025
 * */
@Deprecated("사용안함")
val AwsClient.codeCommit: CodeCommitClient
    get() = getOrCreateClient { CodeCommitClient { awsConfig.build(this) }.regist(awsConfig) }

/** 간단 샘플 */
suspend fun CodeCommitClient.getCommit(repositoryName: String, commitId: String): Commit {
    return this.getCommit {
        this.commitId = commitId
        this.repositoryName = repositoryName
    }.commit!!
}

/**
 * 특정 브랜치의 가장최근 커밋
 * 참고!!! 특정 파일의 커밋 리스트는 API에서 제공하지만, 특정 브랜치의 커밋 리스트는 API로 제공하지 않음.. (이유 모르겠다.. git 명령어로 할수는 있음)
 *  => 이때문에 CICD 배포완료시 최근 X개의 커밋을 서머리해서 보여주는 기능을 만들 수 없음
 *  */
suspend fun CodeCommitClient.getBranchCommit(repositoryName: String, branchName: String): Commit {
    val branch = this.getBranch {
        this.repositoryName = repositoryName
        this.branchName = branchName
    }
    return getCommit(repositoryName, branch.branch!!.commitId!!)
}