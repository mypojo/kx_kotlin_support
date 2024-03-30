package net.kotlinx.okhttp

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType

/**
 * 자주 사용되는거 모음
 * 은근히 공통화가 안되어있음
 * */
object OkHttpMediaType {

    /** JSON */
    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

    /** 이미지 */
    val IMAGE: MediaType = "image/jpeg".toMediaType()

    /** HTML */
    val HTML: MediaType = "text/html".toMediaType()

}
