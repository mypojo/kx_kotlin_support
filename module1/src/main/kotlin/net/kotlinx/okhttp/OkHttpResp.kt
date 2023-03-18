package net.kotlinx.okhttp

import okhttp3.Response

/**
 * 결과 스트림을 닫은 후 리턴하기 위해서 결과 객체를 따로 만든다.
 * httpLog 로 변환 가능
 * */
data class OkHttpResp(
    /** okhttp의 응답 객체*/
    val response: Response,
    /** 텍스트로 변환한 body */
    val respText: String? = null,
) {

    /** 이미지 등의 캐시에 사용됨 */
    val lastModified: String by lazy { response.headers[OkHttpUtil.HRADER_LAST_MODIFIED] ?: "" }

}