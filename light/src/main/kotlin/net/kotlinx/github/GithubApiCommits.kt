package net.kotlinx.github

import net.kotlinx.string.toLocalDateTime
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.LocalDateTime

interface GithubApiCommits {

    @GET("repos/{owner}/{repo}/commits")
    suspend fun getCommits(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 30
    ): List<CommitResponse>


    data class CommitResponse(
        val sha: String,
        val commit: CommitDetails,
        /** 보통 null이 들어옴 */
        val author: GitHubUser?
    )

    data class CommitDetails(
        val message: String,
        val author: CommitAuthor
    )

    data class CommitAuthor(
        val name: String,
        val email: String,
        val date: String
    ) {
        val dateTime: LocalDateTime
            get() = date.toLocalDateTime().plusHours(9)
    }

    data class GitHubUser(
        val login: String,
        val avatarUrl: String
    )

}

