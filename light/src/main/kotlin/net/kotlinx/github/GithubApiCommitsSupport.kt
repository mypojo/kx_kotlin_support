package net.kotlinx.github

import net.kotlinx.github.GithubApiCommits.CommitResponse
import net.kotlinx.string.abbr
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toKr01


fun List<CommitResponse>.printSimple() {
    listOf("sha", "Date", "Author", "Message").toTextGridPrint {
        this.map { arrayOf(it.sha, it.commit.author.dateTime.toKr01(), it.commit.author.name, it.commit.message.abbr(80)) }
    }
}
