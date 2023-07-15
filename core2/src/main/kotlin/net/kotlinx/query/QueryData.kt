package net.kotlinx.query

interface QueryData {
    /** 컬럼 명 */
    var name: String

    /**
     * 해당 컬럼이 있는 테이블들
     * 효율적인 쿼리를 위해서 데이터 양이 적은 테이블이 위로 가야함
     *  */
    var tables: List<String>

    /** 설명 */
    var desc: String

    /** SQL 구문 */
    val format: String
}