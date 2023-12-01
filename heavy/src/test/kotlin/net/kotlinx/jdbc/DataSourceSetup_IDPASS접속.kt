package net.kotlinx.jdbc

import com.vladsch.kotlin.jdbc.session
import com.vladsch.kotlin.jdbc.sqlQuery
import com.vladsch.kotlin.jdbc.using
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class DataSourceSetup_IDPASS접속 : TestRoot() {

    val projectName = "sin"

    @Test
    fun `ID PASS 접속`() {

        val setup = DataSourceSetup {
            dataSourceSetupType = DataSourceSetupType.ID_PASS
            username = "${projectName}_test"
            password = "11h11m123!"
            jdbcUrl = JdbcUrl {
                host = "mysql.dev.11h11m.net"
                database = username
            }
        }
        val dataSource = setup.createDataSource {
            minimumIdle = 1
        }

        using(session(dataSource)) { session ->
            val query = sqlQuery("select member_id,member_name,last_login_time from member limit ?", 3000)
            val queryResult = session.listAny(query)
            queryResult.header.toTextGrid(queryResult.results.map { it.toTypedArray() }).print()

//            val insertQuery: String = "insert into members (name,  created_at) values (?, ?)"
//            session.update(sqlQuery(insertQuery, "Alice", "bb")) // returns effected row count
        }

    }


}