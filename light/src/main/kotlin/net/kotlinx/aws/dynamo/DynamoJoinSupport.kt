package net.kotlinx.aws.dynamo

/** 다이나모 스플릿 */
fun String.ddbSplit(): List<String> = this.split("#")

/**
 * 다이나모 조인
 * 널 제거 & 풀카운트가 아니라면 끝에  # 추가
 *  */
fun Array<*>.ddbJoin(fullCnt: Int? = null): String {
    val path = this.filterNotNull()
    val append = when (fullCnt) {
        null -> ""
        path.size -> ""
        else -> "#"
    }
    return "${path.joinToString("#")}$append"
}
