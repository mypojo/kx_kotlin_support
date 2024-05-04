package net.kotlinx.jdbc

import com.vladsch.kotlin.jdbc.session
import com.vladsch.kotlin.jdbc.sqlQuery
import com.vladsch.kotlin.jdbc.using
import com.zaxxer.hikari.HikariDataSource
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.kotest.BeSpecLog
import org.junit.jupiter.api.Test

class DataSourceSetup_IAM접속 : BeSpecLog(){
    init {
        val projectName = "sin"

        fun doTest(dataSource: HikariDataSource) {

            using(session(dataSource)) { session ->
                val query = sqlQuery("select member_id,member_name,last_login_time from member limit ?", 3000)
                val queryResult = session.listAny(query)
                queryResult.header.toTextGrid(queryResult.results.map { it.toTypedArray() }).print()

                //            val insertQuery: String = "insert into members (name,  created_at) values (?, ?)"
                //            session.update(sqlQuery(insertQuery, "Alice", "bb")) // returns effected row count
            }
        }

        @Test
        fun `IAM - 터널링`() {

            val setup = DataSourceSetup {
                dataSourceSetupType = DataSourceSetupType.IAM
                profile = projectName
                username = "${profile}_dev"
                jdbcUrl = JdbcUrl {
                    host = "${profile}-dev.cluster-yy.ap-northeast-2.rds.amazonaws.com"
                    connectPort = 33061
                    direct = false
                    database = username
                }
            }
            val dataSource = setup.createDataSource {
                minimumIdle = 1
            }

            doTest(dataSource)
        }

        @Test
        fun `ID PASS 접속`() {

            val setup = DataSourceSetup {
                dataSourceSetupType = DataSourceSetupType.ID_PASS
                profile = projectName
                username = "admin"
                password = "xxxxxx "
                jdbcUrl = JdbcUrl {
                    host = "${profile}-dev.cluster-yy.ap-northeast-2.rds.amazonaws.com"
                    connectPort = 33061
                    direct = false
                    database = "${profile}_dev"
                }
            }
            val dataSource = setup.createDataSource {
                minimumIdle = 1
            }

            doTest(dataSource)

        }

        @Test
        fun `ID PASS 접속22`() {

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
}