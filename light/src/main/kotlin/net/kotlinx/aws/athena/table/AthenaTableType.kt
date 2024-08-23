package net.kotlinx.aws.athena.table


/**
 * 일단 EXTERNAL 만 지원
 * */
enum class AthenaTableType(
    /** 생성 스키마에 포함될 명령어 */
    val schema: String
) {

    /**
     * 내부 테이블. 테이블 드랍하면 데이터도 삭제됨..??
     * ex) 아이스버그
     * */
    INTERNAL(""),

    /**
     * 일반적인 테이블
     * ex) S3 업로드 후 쿼리
     * */
    EXTERNAL("EXTERNAL"),
    ;


}
