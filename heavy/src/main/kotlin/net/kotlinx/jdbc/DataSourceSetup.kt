package net.kotlinx.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.kotlinx.aws.AwsConfig
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
     * 프로파일 (로컬 && IAM 방식일경우 필수)
     *  */
    var profile: String? = null

    /** 로그인 유저명 (어떤 인증이든 필수) */
    lateinit var username: String

    /**  비밀번호 SSM (ID/PASS 방식일경우 필수) */
    var password: String by lazyLoadString()

    var region: String = AwsConfig.REGION_KR

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
                profile?.let { System.setProperty("aws.profile", it) } //이게 최선인지는 모르겠음
                val config = HikariConfig().apply {
                    username = this@DataSourceSetup.username
                    jdbcUrl = this@DataSourceSetup.jdbcUrl.url
                    driverClassName = "software.amazon.jdbc.Driver" //AWS Wrapper 전용 드라이버
                    addDataSourceProperty("wrapperPlugins", "iam") //AWS Wrapper 플러그인 설정
                    addDataSourceProperty("iamRegion", region)
                    addDataSourceProperty("iamHost", this@DataSourceSetup.jdbcUrl.host) // IAM 토큰 생성용 실제 RDS 호스트 지정 (터널링 시 필수)
                    addDataSourceProperty("iamDefaultPort", this@DataSourceSetup.jdbcUrl.jdbcDriver.port)  // 실제 RDS 포트
                    addDataSourceProperty("ssl", "true") // SSL 설정 추가
                    addDataSourceProperty("sslmode", "verify-full") // 또는 "require"
                }
                HikariDataSource(config)
            }
        }
    }

}


