package net.kotlinx.jdbc

import net.kotlinx.aws.AwsConfig
import net.kotlinx.file.slash
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.system.ResourceHolder
import javax.sql.DataSource

class TableColumnLoaderTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("TableColumnLoader") {
            val dataSource = koin<DataSource>()
            val awsConfig = koin<AwsConfig>()
            Then("테이블 컬럼 로드") {
                val loader = TableColumnLoader(dataSource, "${awsConfig.profileName}_dev")
                val columns = loader.loadMysqlColumns()
                val jsonText = GsonData.fromObj(columns)
                ResourceHolder.getWorkspace().slash("test").slash("tables.txt").writeText(jsonText.toString())
            }
        }
    }


}