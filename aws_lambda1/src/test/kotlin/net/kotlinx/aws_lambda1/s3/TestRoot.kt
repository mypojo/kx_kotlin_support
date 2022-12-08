package net.kotlinx.aws_lambda1.s3

import mu.KotlinLogging
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import java.io.IOException

/**
 * Junit5기반 테스트를 상속해서 사용하는 도우미
 * 테스트 전에 JVM 옵션이나 환경변수 옵션을 줄 수 있다.
 * 샘플 JVM 옵션 : -Xms128m -Xmx8048m  -Dsetup=TEST
 *
 * 로깅 때문에 core1 에 있어야함
 */
abstract class TestRoot {


    /** 필요시 구현하기  */
    fun beforeDefault() {}

    @BeforeEach
    fun before(testInfo: TestInfo) {
        beforeDefault()
    }

    companion object {

//        /** 디폴트 시간체크  */
//        private var TS: TimeString? = null

        private val log = KotlinLogging.logger {}

        //==================================================== 기본 구현 ======================================================
        @BeforeAll
        fun beforeClass() {
            //LogBackUtil.init("logback-app-dev.xml")
            //GsonStaticInit.init()
            //TS = TimeString()
        }

        /** 매번 테스트 종료시마다 호출된다.  */
        @AfterAll
        @Throws(IOException::class)
        fun afterClass() {
            log.info { "테스트 종료" }
//            Holder.THREAD_INFO.remove()
//            //동일 JVM 내에서 커넥션을 끊으면 다른곳에 문제가 생길 수 있어서 일단 중지
////		AwsInstanceType instanceType = AwsInstanceTypeUtil.getInstanceType();
////		if(instanceType == AwsInstanceType.local){
////			ResourceHolder.finish();
////		}
//            log.info("=== 테스트 종료 (걸린시간 {}) ===", TS)
        }
    }
}