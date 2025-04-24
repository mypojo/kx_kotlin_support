package net.kotlinx.aws.rds

import aws.sdk.kotlin.services.rds.RdsAuthTokenGenerator
import aws.smithy.kotlin.runtime.net.url.Url
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.core.Kdsl
import net.kotlinx.koin.Koins.koinLazy

/**
 * 코틀린용 IAM 데이터소스
 * https://sdk.amazonaws.com/kotlin/api/latest/rds/aws.sdk.kotlin.services.rds/-rds-auth-token-generator/index.html
 *
 * MYSQL 드라이버 / 마리아 드라이버 둘다 가능함.
 * AWS 공식 JDBC 래퍼 드라이버에서 토큰 관리도 해준다는데.. 뭔가 설정을 좀 해야하는듯? 난 잘 안됨
 * https://github.com/aws/aws-advanced-jdbc-wrapper/tree/main?tab=readme-ov-file
 *
 * 코틀린 사용자 다 어디갔나?? 검색해도 안나오고.. ㅠㅠ
 * 생성자 오버라이드 때문에 , 별도 생성자로 구현함
 */
class HikariIamDataSource(@Kdsl block: HikariIamDataSource.() -> Unit = {}) : HikariDataSource() {

    /**
     * 프록시 호스트가 아닌 실제 RDS의 내부 주소
     * ex) xxx.cluster-yyy.ap-northeast-2.rds.amazonaws.com
     */
    lateinit var inputHostname: String

    /** aws 프로파일. 실서버인경우 null로 두면 됨 */
    var profile: String? = null

    /** 실제 DB의 포트  */
    var port: Int = 3306

    /**
     * 인증 토큰을 생성한 후 만료되기 전 x분 동안 유효 -> 디폴트 사용함
     *  */
    override fun getPassword(): String = runBlocking { generator.generateAuthToken(Url.parse("https://$inputHostname:${port}"), awsConfig.region, username) }

    init {
        block(this)
    }

    //==================================================== 블록 이후의 설정 (위험하다.. 일반 lzay로 수정하자) ======================================================

    /** AWS 설정 */
    private val awsConfig by koinLazy<AwsConfig>(profile)

    /** 생성기 */
    private val generator by lazy { RdsAuthTokenGenerator(awsConfig.credentialsProvider) }


}