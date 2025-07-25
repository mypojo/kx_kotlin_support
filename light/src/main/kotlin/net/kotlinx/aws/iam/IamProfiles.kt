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
    fun findProfileByAwsId(prefix: String, suff: String? = null): String = profiles.filter {
        if (suff == null) it.second.startsWith(prefix)
        else it.second.startsWith(prefix) && it.second.endsWith(suff)
    }.run {
        check(this.size == 1) { "값이 없거나 중복된 값 -> ${prefix}" }
        first().first
    }

    /** 간단출력 */
    fun printProfiles() {
        listOf("index", "profileName", "awsId").toTextGridPrint {
            profiles.mapIndexed { i, it -> arrayOf(i, it.first, it.second) }
        }
    }
}