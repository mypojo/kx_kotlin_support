//package net.kotlinx.aws.javaSdkv2
//
//import mu.KotlinLogging
//import net.kotlinx.core.Kdsl
//import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
//import software.amazon.awssdk.regions.Region
//import software.amazon.awssdk.services.rds.RdsClient
//import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest
//import com.zaxxer.hikari.HikariDataSource as HikariDataSource1
//
///**
// * 문서확인!! mysql 기본 드라이버만 되는듯.  ex) jdbc:mysql://localhost:33061/${name}_dev
// * https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.IAMDBAuth.Connecting.html
// */
//@Deprecated("코틀린 버전 사용하세요")
//class HikariIamDataSource(@Kdsl block: HikariIamDataSource.() -> Unit = {}) : HikariDataSource1() {
//
//    private val log = KotlinLogging.logger {}
//
//    /**
//     * 프록시 호스트가 아닌 실제 RDS의 내부 주소
//     * ex) xxx.cluster-yyy.ap-northeast-2.rds.amazonaws.com
//     */
//    lateinit var inputHostname: String
//
//    /** aws 프로파일. 실서버인경우 null로 두면 됨 */
//    var profile: String? = null
//
//    /** 실제 DB의 포트  */
//    var port: Int = 3306
//
//
//    val rdsClient: RdsClient by lazy {
//        log.info { "RDS 데이터소스 생성. profile ($profile)" }
//        val credentialsProvider = profile?.let { ProfileCredentialsProvider.create(it) }
//        RdsClient.builder()
//            .region(Region.AP_NORTHEAST_2)
//            .credentialsProvider(credentialsProvider)
//            .build()
//    }
//
//    override fun getPassword(): String {
//        val rdsUtilities = rdsClient.utilities() //코틀린 버전에는 이 함수가 없음
//        val request = GenerateAuthenticationTokenRequest.builder()
//            .region(Region.AP_NORTHEAST_2)
//            .username(this.username)
//            .hostname(inputHostname)
//            .port(port)
//            .build()
//        return rdsUtilities.generateAuthenticationToken(request)
//    }
//
//    init {
//        block(this)
//    }
//
//}