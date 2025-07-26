package net.kotlinx.domain.query

import net.kotlinx.core.Kdsl


/**
 * 단일 테이블 대상의 비정형 쿼리를 위한 디멘션
 * 가능한 테이블중 가장 위에 있는것을 선택한다.
 * */
class QueryDimension : QueryData {

    @Kdsl
    constructor(block: QueryDimension.() -> Unit = {}) {
        apply(block)
    }

    /** 컬럼 명 */
    override var name: String = ""

    /** 해당 컬럼이 있는 테이블들 */
    override var tables: List<String> = emptyList()

    /** 설명 */
    override var desc: String = ""

    override val format: String
        get() = name

}
