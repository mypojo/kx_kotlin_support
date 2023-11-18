package net.kotlinx.jdbc

import com.vladsch.kotlin.jdbc.Session
import com.vladsch.kotlin.jdbc.SqlQuery
import java.sql.ResultSetMetaData

/**
 * 쿼리 결과
 * https://github.com/vsch/kotlin-jdbc
 *  */
data class QueryResult(val header: List<String>, val results: List<List<Any?>>)

/**
 * any로 변환해서 리턴
 * 주의!! batchInsert가 안됨!! -> jdbc 말고 expose 사용할것
 *  */
fun Session.listAny(query: SqlQuery): QueryResult {
    lateinit var metaData: ResultSetMetaData
    val results = this.list(query) { row ->
        metaData = row.metaDataOrNull()
        (1..metaData.columnCount).map { row.anyOrNull(it) }
    }
    val headers = (1..metaData.columnCount).map { metaData.getColumnName(it) }
    return QueryResult(headers, results)
}
