package net.kotlinx.dooray.drive

/**
 * Dooray API의 공통 응답 형식
 */
data class DoorayDriveResponse<T>(
    val header: DoorayHeader,
    val result: T?,
    val totalCount: Int? = null,
) {
    /**
     * Dooray API 응답 헤더
     */
    data class DoorayHeader(
        val isSuccessful: Boolean,
        val resultCode: Int,
        val resultMessage: String,
    )
}