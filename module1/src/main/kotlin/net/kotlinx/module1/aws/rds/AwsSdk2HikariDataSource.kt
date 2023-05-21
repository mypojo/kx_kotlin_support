package net.kotlinx.module1.aws.rds

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain
import software.amazon.awssdk.services.rds.RdsClient
import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest

/**
 * https://ordina-jworks.github.io/cloud/2022/06/13/aws-rds-iam-authentication-spring-boot.html
 * 거의 복붙 했음
 */
class AwsSdk2HikariDataSource(
    /** DB 계정 명 */
    private val inputUsername: String,
    /** JDBC 주소 */
    inputJdbcUrl: String,
    /**
     * 프록시 호스트가 아닌 실제 RDS의 내부 주소
     * ex) xxx.cluster-yyy.ap-northeast-2.rds.amazonaws.com
     */
    private val inputHostname: String,
    private val profile: String? = null,
    /** 실제 DB의 포트  */
    private val port: Int = 3306,
) : HikariDataSource() {

    private val log = KotlinLogging.logger {}

    init {
        jdbcUrl = inputJdbcUrl
        username = inputUsername
    }

    val rdsClient: RdsClient by lazy {
        log.info { "RDS 데이터소스 생성. profile ($profile)" }
        val credentialsProvider = profile?.let { ProfileCredentialsProvider.create(it) }
        RdsClient.builder()
            .region(DefaultAwsRegionProviderChain().region)
            .credentialsProvider(credentialsProvider)
            .build()
    }

    override fun getPassword(): String {
        log.info { "RDS 데이터소스 password 생성" }
        val rdsUtilities = rdsClient.utilities()
        val request = GenerateAuthenticationTokenRequest.builder()
            .username(inputUsername)
            .hostname(inputHostname)
            .port(port)
            .build()
        return rdsUtilities.generateAuthenticationToken(request)
    }

}