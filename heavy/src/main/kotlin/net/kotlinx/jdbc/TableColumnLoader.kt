package net.kotlinx.jdbc

import com.vladsch.kotlin.jdbc.session
import com.vladsch.kotlin.jdbc.sqlQuery
import com.vladsch.kotlin.jdbc.using
import javax.sql.DataSource

data class TableColumn(
    val tableName: String,
    val columnName: String,
    val columnType: String,
    val nullable: Boolean,
    val columnComment: String? = null,
)

/** 테이블 컬럼정보 로드기 */
class TableColumnLoader(private val dataSource: DataSource, val schema: String) {

    fun loadMysqlColumns(): List<TableColumn> {
        return using(session(dataSource)) { session ->

            val sql = """
                            SELECT a.TABLE_NAME,b.COLUMN_NAME,b.COLUMN_TYPE,b.IS_NULLABLE,b.COLUMN_COMMENT
                            FROM information_schema.tables a join information_schema.COLUMNS b
                                on a.TABLE_NAME = b.TABLE_NAME
                            where a.table_schema = ?
                              and a.table_name not in ('system_param')
                            order by a.TABLE_NAME,b.ORDINAL_POSITION
            """.trimIndent()

            val query = sqlQuery(sql, schema)
            val queryResult = session.listAny(query)
            queryResult.results.map {
                TableColumn(it[0].toString(), it[1].toString(), it[2].toString(), it[3].toString().toBoolean(), it[4]?.toString())
            }

        }
    }


}


