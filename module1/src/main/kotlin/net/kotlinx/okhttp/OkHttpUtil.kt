package net.kotlinx.okhttp

import okhttp3.MediaType.Companion.toMediaType

object OkHttpUtil {

    /** 디폴트 미디어타입 */
    val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

    /** 미디어타입 이미지 */
    val MEDIA_TYPE_IMAGE = "image/jpeg".toMediaType()

    /**  베이스 버퍼 사이즈  */
    const val BUFFER_SIZE = 4096

    //==================================================== 헤더 ======================================================

    const val HRADER_LAST_MODIFIED = "Last-Modified"
    const val HRADER_IF_MODIFIED = "If-Modified-Since"

}
