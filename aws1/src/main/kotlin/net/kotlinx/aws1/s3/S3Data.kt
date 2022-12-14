package net.kotlinx.aws1.s3

import net.kotlinx.core1.string.encodeUrl

/**
 * S3 패스가 다양하게 사용됨
 * 풀 네임 사용할때도 있고, 버킷과 키를 따로 사용할때도 있다. 이것을 하나로 관리
 */
data class S3Data(
    val bucket: String,
    var key: String,
) {

    /**
     * 업로드 파일에 접근 가능한 S3 프로토콜을 리턴한다. 이 URL은 다운로드나 스파크 등  내부 API에서 사용된다.
     * https://gpdb.docs.pivotal.io/4390/admin_guide/load/topics/g-s3-protocol.html
     */
    fun toFullPath(): String = arrayOf(PROTO, bucket, key).joinToString("/")

    fun toPath(): String = arrayOf(bucket, key).joinToString("/")

    /**
     * 다운로드 링크를 리턴한다. (서울 기준 고정!!)
     * 퍼블릭일경우만 해당 (IP 화이트리스트 체크할것!!)
     */
    fun toPublicLink(region: String = "ap-northeast-2"): String = "https://${bucket}.s3.${region}.amazonaws.com/${key.encodeUrl()}"

    companion object {

        /** 프로토콜.  주의! /가 1개 뿐이다. 나머지 1개는 연결시 붙여준다.    */
        var PROTO = "s3:/"

        //========================================== static ==================================================
        /**  양쪽 / 에 안전하다.  */
        fun parse(fullPath: String): S3Data = when {
            fullPath.startsWith("s3://") -> {
                val text = fullPath.substring(5)
                S3Data(text.substringBefore("/"), text.substringAfter("/"))
            }

            else -> throw IllegalArgumentException()
        }
    }
}