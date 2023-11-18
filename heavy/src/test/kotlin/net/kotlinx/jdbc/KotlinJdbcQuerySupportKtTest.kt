package net.kotlinx.jdbc

import com.vladsch.kotlin.jdbc.session
import com.vladsch.kotlin.jdbc.sqlQuery
import com.vladsch.kotlin.jdbc.using
import net.kotlinx.aws.javaSdkv2.HikariIamDataSource
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test

class KotlinJdbcQuerySupportKtTest : TestLight() {

    @Test
    fun test() {

        val name = "sin"
        val dataSource = HikariIamDataSource {
            profile = name
            inputHostname = "${profile}-dev.cluster-cxoltcrf7s91.ap-northeast-2.rds.amazonaws.com"
            username = "${name}_dev"
            jdbcUrl = "jdbc:mysql://localhost:33061/${name}_dev"
        }

        using(session(dataSource)) { session ->
            val query = sqlQuery("select member_id,member_name,last_login_time from member limit ?", 3000)
            val queryResult = session.listAny(query)
            queryResult.header.toTextGrid(queryResult.results.map { it.toTypedArray() }).print()

            val insertQuery: String = "insert into members (name,  created_at) values (?, ?)"
            session.update(sqlQuery(insertQuery, "Alice", "bb")) // returns effected row count
        }

    }

}