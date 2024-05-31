package net.kotlinx.aws.iam

import net.kotlinx.string.toTextGridPrint

/**
 * STS 프로파일 정보 모음
 * 시크릿 관련 정보는 빠짐
 *  */
data class IamProfiles(
    /** 프로파일들. <프로파일/AWS ID>  */
    val profiles: List<Pair<String, String>>
) {
    /**
     * AWS ID 접두어로 조회한다.
     * ex) val profileName = koin<IamProfiles>().findProfileByAwsId("99")
     *  */
    fun findProfileByAwsId(id: String): String = profiles.filter { it.second.startsWith(id) }.run {
        check(this.size == 1) { "중복된 id값 발견 -> $this" }
        first().first
    }

    /** 간단출력 */
    fun printProfiles() {
        listOf("index", "profileName", "awsId").toTextGridPrint {
            profiles.mapIndexed { i, it -> arrayOf(i, it.first, it.second) }
        }
    }
}