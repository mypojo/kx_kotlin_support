package net.kotlinx.module.jdbc

import com.vladsch.kotlin.jdbc.session
import com.vladsch.kotlin.jdbc.sqlQuery
import com.vladsch.kotlin.jdbc.using
import net.kotlinx.aws.javaSdkv2.HikariIamDataSource
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test

class KotlinJdbcQuerySupportKtTest : TestRoot() {

    @Test
    fun test() {

        val name = "sin"
        val dataSource = HikariIamDataSource("${name}-dev.cluster-cxoltcrf7s91.ap-northeast-2.rds.amazonaws.com", "$name")
        dataSource.username = "${name}_dev"
        dataSource.jdbcUrl = "jdbc:mysql://localhost:33061/${name}_dev"

        using(session(dataSource)) { session ->
            val query = sqlQuery("select member_id,member_name,last_login_time from member limit ?", 3)
            val queryResult = session.listAny(query)
            queryResult.header.toTextGrid(queryResult.results.map { it.toTypedArray() }).print()
        }


    }

}