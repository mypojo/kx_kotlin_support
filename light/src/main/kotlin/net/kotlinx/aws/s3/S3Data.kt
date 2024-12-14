package net.kotlinx.aws.s3

import net.kotlinx.string.encodeUrl

/**
 * S3 패스가 다양하게 사용됨. 이를 명시적으로 합쳐서 다루기 위한 객체.
 * 풀 네임 사용할때도 있고, 버킷과 키를 따로 사용할때도 있다. 이것을 하나로 관리
 */
data class S3Data(
    val bucket: String,
    var key: String,
) {

    /** 버전이 있는경우. 주로 삭제할때 사용  */
    var versionId: String? = null

    /** 파일명 추출 */
    val fileName: String
        get() = key.substringAfterLast("/")

    /** 부모 디렉토리 */
    val parent: S3Data
        get() = S3Data(bucket, key.substringBeforeLast("/"))

    /** 디렉토리인지 */
    val isDirectory: Boolean
        get() = key.endsWith("/")

    /**
     * 업로드 파일에 접근 가능한 S3 프로토콜을 리턴한다. 이 URL은 다운로드나 스파크 등  내부 API에서 사용된다.
     * https://gpdb.docs.pivotal.io/4390/admin_guide/load/topics/g-s3-protocol.html
     */
    fun toFullPath(): String = arrayOf(PROTO, bucket, key).joinToString("/")

    /** 끝애 /가 있는 디렉토리 형태로 전달 */
    fun toFullPathDir(): String = arrayOf(PROTO, bucket, key).joinToString("/") + "/"

    /** 프로토콜이 없는 일반 path */
    fun toPath(): String = arrayOf(bucket, key).joinToString("/")

    /**
     * 다운로드 링크를 리턴한다. (서울 기준 고정!!)
     * 퍼블릭일경우만 해당 (IP 화이트리스트 체크할것!!)
     */
    fun toPublicLink(region: String = "ap-northeast-2"): String = "https://${bucket}.s3.${region}.amazonaws.com/${key.encodeUrl()}"

    /** file 처럼, 경로 추가 */
    fun slash(append: String): S3Data = S3Data(this.bucket, "${this.key}/${append}")

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