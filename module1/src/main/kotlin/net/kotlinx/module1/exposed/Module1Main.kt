package net.kotlinx.module1.exposed

import com.zaxxer.hikari.HikariConfig


fun main(){

    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:mysql://localhost:3366/exposed_study?useSSL=false&serverTimezone=UTC&autoReconnect=true&rewriteBatchedStatements=true"
        driverClassName = "com.mysql.cj.jdbc.Driver"
        username = "root"
        password = ""
    }


}