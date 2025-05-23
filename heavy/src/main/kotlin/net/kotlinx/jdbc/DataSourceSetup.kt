package net.kotlinx.jdbc

import com.zaxxer.hikari.HikariDataSource
import net.kotlinx.aws.rds.HikariIamDataSource
import net.kotlinx.core.Kdsl
import net.kotlinx.lazyLoad.lazyLoadString
import org.koin.core.component.KoinComponent

class DataSourceSetup : KoinComponent {

    @Kdsl
    constructor(block: DataSourceSetup.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 환경정보 ======================================================

    /** 타입 */
    lateinit var dataSourceSetupType: DataSourceSetupType

    //==================================================== 설정정보 ======================================================

    /** jdbcUrl */
    lateinit var jdbcUrl: JdbcUrl

    //==================================================== 인증정보 ======================================================

    /**
     * 프로파일 (로컬 & IAM 방식일경우 필수)
     *  */
    var profile: String? = null

    /** 로그인 유저명 (어떤 인증이든 필수) */
    lateinit var username: String

    /**  비밀번호 SSM (ID/PASS 방식일경우 필수) */
    var password: String by lazyLoadString()

    /** 생성 */
    fun createDataSource(block: HikariDataSource.() -> Unit = {}): HikariDataSource {
        return when (dataSourceSetupType) {
            DataSourceSetupType.ID_PASS -> {
                HikariDataSource().apply {
                    username = this@DataSourceSetup.username
                    password = this@DataSourceSetup.password
                    jdbcUrl = this@DataSourceSetup.jdbcUrl.url
                    block()
                }
            }

            DataSourceSetupType.IAM -> {
                HikariIamDataSource {
                    profile = this@DataSourceSetup.profile
                    inputHostname = this@DataSourceSetup.jdbcUrl.host
                    username = this@DataSourceSetup.username
                    jdbcUrl = this@DataSourceSetup.jdbcUrl.url
                    block()
                }
            }
        }
    }

}


