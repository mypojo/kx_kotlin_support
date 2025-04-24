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
import net.kotlinx.lazyLoad.lazyLoadStringSsm
import net.kotlinx.string.toTextGrid


class DataSourceSetupTest : BeSpecHeavy() {


    init {
        initTest(KotestUtil.IGNORE)

        Given("DataSourceSetup") {

            val projectName = findProfile97

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
                    username = "${profile}_prod"
                    jdbcUrl = JdbcUrl {
                        host = "main-prod.cluster-cfs4usogashn.ap-northeast-2.rds.amazonaws.com"
                        connectPort = 33061
                        direct = false
                        database = username
                    }
                }
                log.info { "JDBC URL : ${setup.jdbcUrl.url}" }
                val dataSource = setup.createDataSource {
                    minimumIdle = 1
                }
                doTest(dataSource)
            }

            Then("ID PASS 접속 - 터널링 (DB admin)") {
                val client = koin<AwsClient>(projectName)
                val secret = client.smStore["main-prod"].toGsonData()
                val setup = DataSourceSetup {
                    dataSourceSetupType = DataSourceSetupType.ID_PASS
                    profile = projectName
                    username = "admin"
                    password = secret["password"].str!!
                    jdbcUrl = JdbcUrl {
                        host = "main-prod.cluster-cfs4usogashn.ap-northeast-2.rds.amazonaws.com"
                        connectPort = 33061
                        direct = false
                        database = "${profile}_prod"
                    }
                }

                val dataSource = setup.createDataSource {
                    minimumIdle = 1
                }

                doTest(dataSource)
            }

            Then("ID PASS 접속 - 직접접속 (공용 테스트서버)") {
                val pwd by lazyLoadStringSsm("/rds/secret/dev", projectName)
                val setup = DataSourceSetup {
                    dataSourceSetupType = DataSourceSetupType.ID_PASS
                    username = "${projectName}"
                    password = pwd
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
                }
            }
        }
    }

}