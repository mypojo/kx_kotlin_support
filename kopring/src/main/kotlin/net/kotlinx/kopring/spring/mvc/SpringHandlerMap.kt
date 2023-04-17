package net.kotlinx.kopring.spring.mvc

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method

data class SpringUrl(
    var url: String,
    var clazz: Class<*>,
    var method: Method,
)

/**
 * 스프링용 MVC 매핑 데이터 저장
 */
class SpringHandlerMap(
    private val handlerMapping: RequestMappingHandlerMapping
) {

    /** rest를 사용하기도 함으로 url을 변형하지 않고 그냥 다 보여준다.
     * 프로젝트마다 변형해서 사용할것   */
    private val urlMap: Map<String, SpringUrl> by lazy {
        handlerMapping.handlerMethods.entries.flatMap { (req, value) ->
            req.patternsCondition!!.patterns.map {
                SpringUrl(
                    it,
                    value.beanType,
                    value.method,
                )
            }
        }.associateBy { it.url }
    }

    operator fun get(url: String): SpringUrl? = urlMap[url]

}
