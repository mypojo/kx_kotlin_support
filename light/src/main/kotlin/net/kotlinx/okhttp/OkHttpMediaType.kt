package net.kotlinx.okhttp

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType

/**
 * 자주 사용되는거 모음.
 * 은근히 공통화가 안되어있음
 * */
object OkHttpMediaType {

    /** 헤더 키값 */
    const val KEY = "Content-Type"

    /** JSON */
    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

    /** 이미지 */
    val IMAGE: MediaType = "image/jpeg".toMediaType()

    /** HTML */
    val HTML: MediaType = "text/html".toMediaType()

    /** multipart */
    val MULTIPART: MediaType = "multipart/form-data".toMediaType()

    /** octet-stream */
    val STREAM: MediaType = "application/octet-stream".toMediaType()

    /** FORM */
    val FORM_URLENCODED: MediaType = "application/x-www-form-urlencoded".toMediaType()

}
