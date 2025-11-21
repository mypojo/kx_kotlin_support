package net.kotlinx.jdbc

import net.kotlinx.awscdk.network.PortUtil


enum class JdbcDriver(
    /** 드라이버 이름 */
    val jdbcName: String,
    /** 기본 포트 */
    val port: Int,
) {

    //==================================================== MYSQL ======================================================

    MARIADB("mariadb", PortUtil.MYSQL),
    MYSQL("mysql", PortUtil.MYSQL),
    MYSQL_AWS("mysql:aws", PortUtil.MYSQL),

    //==================================================== POSTGRESQL ======================================================

    POSTGRESQL("postgresql", PortUtil.POSTGRESQL), //바닐라버전
    ;
}

