package net.kotlinx.jdbc

import net.kotlinx.collection.toQueryString
import net.kotlinx.core.Kdsl
import net.kotlinx.lazyLoad.lazyLoadString

class JdbcUrl {

    @Kdsl
    constructor(block: JdbcUrl.() -> Unit = {}) {
        apply(block)
    }

    /** 터널링(배스천) 사용인경우 false로*/
    var direct: Boolean = true

    /** 엔드포인트 주소 or SSM 경로 */
    var host: String by lazyLoadString()

    /** JDBC URL 뒤에 붙는 그거 */
    lateinit var database: String

    /**
     * 실제 접속시 포트
     * 로컬인경우 터널링 포트
     *  */
    var connectPort: Int = 3306

    /** 드라이버 */
    var jdbcDriver: JdbcDriver = JdbcDriver.MYSQL

    /**
     * 옵션
     * https://2ssue.github.io/programming/HikariCP-MySQL/
     *  */
    var option: Map<String, String> = mapOf(
        /** 대량의 데이터 insert시 쿼리 변경  */
        "rewriteBatchedStatements" to "true",
        /** 캐시 온 */
        "cachePrepStmts" to "true",
        "prepStmtCacheSize" to "250",
        "prepStmtCacheSqlLimit" to "2048",
        /** 서버측 PreparedStatement 사용  */
        "useServerPrepStmts" to "true",
        //이하 인증관련 옵션.. 필요 없는듯
//        "allowPublicKeyRetrieval" to "true",
//        "useSSL" to "true",
//        "sslMode" to "REQUIRED",
//        "authenticationPlugins" to "com.mysql.cj.protocol.a.authentication.MysqlClearPasswordPlugin",
    )

    /** 배스천 호스트 고려한 jdbcUrl  */
    val url: String
        get() {
            val connectHost = if (direct) host else "localhost"
            return "jdbc:${jdbcDriver.name.lowercase()}://${connectHost}:${connectPort}/${database}?${option.toQueryString()}"
        }

}