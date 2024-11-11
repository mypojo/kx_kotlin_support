package net.kotlinx.domain.ddb

/**
 * 인덱스 정의
 * */
enum class DbMultiIndex {

    GSI01,
    GSI02,
    GSI03,
    GSI04,
    ;

    /** 인덱스 이름 (소문자) */
    val logicalName: String
        get() = this.name.lowercase()

    val pkName: String
        get() = "${logicalName}_pk"

    val skName: String
        get() = "${logicalName}_sk"

    /** 인덱스 이름 (소문자) */
    val indexName: String
        get() = "gidx-${pkName}-${skName}"

}