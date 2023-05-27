package net.kotlinx.module.aws.rds

import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test

class AwsSdk2HikariDataSourceTest : TestRoot() {

    @Test
    fun test() {

        val name = "sin"
        val dataSource = AwsSdk2HikariDataSource(
            "${name}_dev",
            "jdbc:mariadb://localhost:33061/${name}_dev",
            "$name-dev.cluster-cxoltcrf7s91.ap-northeast-2.rds.amazonaws.com",
            "$name",
        )

        println(dataSource.password)

    }

}