package net.kotlinx.spring

import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.time.TimeStart
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContext
import kotlin.reflect.KClass

/**
 * 람다에서 부트 늦은로딩을 하기 위해서 만들었음
 *
 * kotest 등에서 강제 실행시 JVM 옵션으로 프로파일 설정 필요!!
 * ex) -Dspring.profiles.active=default,dev,local
 */
class SpringBootContext {

    private val log = KotlinLogging.logger {}

    @Kdsl
    constructor(block: SpringBootContext.() -> Unit = {}) {
        apply(block)
    }

    /** 부트 클래스 정보 */
    lateinit var bootClass: KClass<*>

    /**
     * 백그라은드 로직 = NONE
     * 웹 로직 = SERVLET  -> 컨트롤러 등의 웹 의존성 같이 주입됨
     * */
    var applicationType: WebApplicationType = WebApplicationType.NONE

    /**
     * 부트 컨텍스트
     * 실세 부트 실행시 넣어줌
     *  */
    var applicationContext: ApplicationContext? = null

    /**
     * 부트 없으면 부트 실행
     *  */
    fun getOrCreatecontext(): ApplicationContext {
        return applicationContext ?: run {
            log.debug { " -> 스프링 부트 강제 초기화 시작" }
            val ts = TimeStart()
            val context = SpringApplicationBuilder(bootClass.java).web(applicationType).run()
            log.info { " -> 스프링 부트 강제 초기화 완료.. $ts" }
            context
        }

    }

}
