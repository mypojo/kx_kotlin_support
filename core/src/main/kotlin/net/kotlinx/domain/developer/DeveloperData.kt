package net.kotlinx.domain.developer

data class DeveloperData(
    val id: String,
    val name: String? = null,
    val ip: String? = null,
    val slackId: String? = null,
    val email: String? = null,
    /** 깃 ID.  커밋으로 슬랙 호출할때 사용  */
    val gitId: String? = null,
)