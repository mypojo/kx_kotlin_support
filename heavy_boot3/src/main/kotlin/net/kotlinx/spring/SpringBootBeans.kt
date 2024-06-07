package net.kotlinx.spring

import mu.KotlinLogging
import net.kotlinx.string.decapital
import org.springframework.context.ApplicationContext

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


}
