package net.kotlinx.spring

import mu.KotlinLogging
import net.kotlinx.string.decapital
import net.kotlinx.time.TimeStart
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContext
import org.springframework.web.context.ContextLoader
import kotlin.reflect.KClass

/**
 * 람다에서 부트 늦은로딩을 하기 위해서 만들었음
 * 코드에서 SpringUtil 직접 사용금지
 */
class SpringBootBeanLoader(private val bootClass: KClass<*>) {

    private val log = KotlinLogging.logger {}

    /** was 초기화 or junit 등 여러가지에서 활용 가능하도록 열어둠 */
    private val context: ApplicationContext by lazy {
        val exist = ContextLoader.getCurrentWebApplicationContext()
        if (exist == null) {
            val ts = TimeStart()
            val context = SpringApplicationBuilder(bootClass.java).web(WebApplicationType.NONE).run()
            log.info { " -> 스프링 부트 초기화 완료.. $ts" }
            context
        } else {
            log.info { " -> 이미 존재하는 컨텍스트 리턴" }
            exist
        }
    }

    //==================================================== 메소드들 ======================================================

    /** id로 가져옴 */
    fun <T> getBean(beanName: String): T = context.getBean(beanName) as T

    /** 클래스명으로 가져옴 */
    inline fun <reified T> getBean(): T = getBean(T::class.simpleName!!.decapital())


}
