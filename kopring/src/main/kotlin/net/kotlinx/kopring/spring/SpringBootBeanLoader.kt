package net.kotlinx.kopring.spring

import mu.KotlinLogging
import net.kotlinx.core.time.TimeStart
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContext
import org.springframework.web.context.ContextLoader

/**
 * 람다에서 부트 늦은로딩을 하기 위해서 만들었음
 * 코드에서 SpringUtil 직접 사용금지
 */
class SpringBootBeanLoader(
    /** 동적 로드시 반드시 설정되어 있어야함 */
    private val bootClassNames: List<String>
) {

    private val log = KotlinLogging.logger {}

    private fun loadBootClass(): Class<*> {
        for (className in bootClassNames) {
            try {
                return Class.forName(className)
            } catch (e: Exception) {
                //아무것도 하지않음
            }
        }
        throw IllegalStateException("부트클래스를 찾을 수 없습니다. from $bootClassNames")
    }

    /** was 초기화 or junit 등 여러가지에서 활용 가능하도록 열어둠 */
    private val context: ApplicationContext by lazy {

        //부트가 올라가있으면 컨텍스트에서 로드가 될거임
        ContextLoader.getCurrentWebApplicationContext()?.let {
            log.info { " -> 이미 로딩된 부트에서 컨텍스트 로드" }
            return@lazy it
        }

        run {
            val ts = TimeStart()

            bootClassNames.map {

            }

            val clazz = loadBootClass()
            SpringApplicationBuilder(clazz).web(WebApplicationType.NONE).run().apply {
                log.info { " -> 스프링 부트 초기화 완료.. $ts" }
            }
        }
    }

    //==================================================== 메소드들 ======================================================

    /** 빈 가져옴 */
    fun <T> getBean(beanName: String): T = context.getBean(beanName) as T

    /** 클래스명으로 가져옴 */
    fun <T> getBean(clazz: Class<T>): T = getBean(clazz.simpleName.replaceFirstChar { it.lowercase() })

//    /** DAO, 리파지토리 등 제러릭 붙은 컨테이너 내의 빈을 찾아준다.  */
//    fun <T> getGenericBean(abstractType: Class<T>, genrricType: Class<*>): T? {
//        val beans = context.getBeansOfType(abstractType)
//        for (bean in beans.values) {
//            val clazz: Class<*> = GenericUtil.genericClass(bean.javaClass, 0)
//            if (genrricType == clazz) return bean
//        }
//        return null
//    }


}
