package net.kotlinx.spring

import mu.KotlinLogging
import net.kotlinx.string.decapital
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment

/**
 * 람다에서 부트 늦은로딩을 하기 위해서 만들었음
 */
class SpringBootBeans(val context: ApplicationContext) {

    private val log = KotlinLogging.logger {}

    //==================================================== 메소드들 ======================================================

    /** id로 가져옴 */
    fun <T> getBean(beanName: String): T = context.getBean(beanName) as T

    /** 클래스명으로 가져옴 */
    inline fun <reified T> getBean(): T = getBean(T::class.simpleName!!.decapital())

    //==================================================== 프로퍼티들 ======================================================

    /**
     * 스프링 yaml 로 설정하는 프로퍼티 (클래스 이름은 Environment 임.. 헷갈림 주의)
     * ex) env["spring.aa.bb"]
     *  */
    val props: Environment by lazy { getBean<Environment>() }

    /**
     * 활성화된 프로파일들
     *  */
    val profiles: Set<String> by lazy { props.activeProfiles.toSet() }


}
