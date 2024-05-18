package net.kotlinx.jdbc

import net.kotlinx.aws.AwsConfig
import net.kotlinx.file.slash
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.system.ResourceHolder
import javax.sql.DataSource

class TableColumnLoaderTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("TableColumnLoader") {
            val dataSource by koinLazy<DataSource>()
            val awsConfig by koinLazy<AwsConfig>()
            Then("테이블 컬럼 로드") {
                val loader = TableColumnLoader(dataSource, "${awsConfig.profileName}_dev")
                val columns = loader.loadMysqlColumns()
                val jsonText = GsonData.fromObj(columns)
                ResourceHolder.getWorkspace().slash("test").slash("tables.txt").writeText(jsonText.toString())
            }
        }
    }


}