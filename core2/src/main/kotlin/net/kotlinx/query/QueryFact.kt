package net.kotlinx.query

/**
 * 단일 테이블 대상의 비정형 쿼리를 위한 매트릭 조합
 * 가능한 테이블중 가장 위에 있는것을 선택한다.
 * */
class QueryFact(block: QueryFact.() -> Unit = {}): net.kotlinx.query.QueryData {
    /** 컬럼 명 */
    override var name: String = ""

    /** 해당 컬럼이 있는 테이블들 */
    override var tables: List<String> = emptyList()

    /** 설명 */
    override var desc: String = ""

    /** 없으면 matric 취급 */
    var dataType: net.kotlinx.query.QueryFactType = net.kotlinx.query.QueryFactType.SUM

    override val format:String
        get()  = dataType.format(name)

    init {
        block(this)
    }
}