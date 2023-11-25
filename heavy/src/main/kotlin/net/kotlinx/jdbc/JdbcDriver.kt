package net.kotlinx.jdbc


enum class JdbcDriver(val jdbcName: String) {

    MARIADB("mariadb"),
    MYSQL("mysql"),
    MYSQL_AWS("mysql:aws"),
    ;
}

