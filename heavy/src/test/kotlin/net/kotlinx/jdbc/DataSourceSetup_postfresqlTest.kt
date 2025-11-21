package net.kotlinx.jdbc

import com.vladsch.kotlin.jdbc.session
import com.vladsch.kotlin.jdbc.sqlQuery
import com.vladsch.kotlin.jdbc.using
import com.zaxxer.hikari.HikariDataSource
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.sm.smStore
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.toTextGrid


class DataSourceSetup_postfresqlTest : BeSpecHeavy() {


    init {
        initTest(KotestUtil.IGNORE)

        Given("DataSourceSetup") {

            val projectName = findProfile49

            val client = koin<AwsClient>(projectName)
            val secret = client.smStore["main-prod"].toGsonData()
            val port = 54321

            fun doTest(dataSource: HikariDataSource) {
                using(session(dataSource)) { session ->
                    val query = sqlQuery("select member_id,member_name,last_login_time from member limit ?", 3000)
                    val queryResult = session.listAny(query)
                    queryResult.header.toTextGrid(queryResult.results.map { it.toTypedArray() }).print()
                }
            }

            Then("IAM - 터널링") {
                val setup = DataSourceSetup {
                    dataSourceSetupType = DataSourceSetupType.IAM
                    profile = projectName
                    username = "${projectName}-dev"
                    jdbcUrl = JdbcUrl {
                        host = secret["host"].str!!
                        connectPort = port
                        direct = false
                        database = "${projectName}-dev"
                    }
                }
                log.info { "JDBC URL : ${setup.jdbcUrl.url}" }
                val dataSource = setup.createDataSource {
                    minimumIdle = 1
                }
                doTest(dataSource)
            }

            Then("ID PASS 접속 - 터널링 (DB admin)") {
                val setup = DataSourceSetup {
                    dataSourceSetupType = DataSourceSetupType.ID_PASS
                    username = secret["username"].str!!
                    password = secret["password"].str!!
                    jdbcUrl = JdbcUrl {
                        host = secret["host"].str!!
                        connectPort = port
                        direct = false
                        database = "${projectName}-dev"
                        option = emptyMap()
                    }
                    log.debug { " -> url : ${jdbcUrl.url}" }
                    //jdbc:postgresql://localhost:54321/dmp_dev?
                    //jdbc:aws-wrapper:postgresql://localhost:54321/dmp-prod
                }

                val dataSource = setup.createDataSource {
                    minimumIdle = 1
                }

                doTest(dataSource)
            }
        }
    }

}