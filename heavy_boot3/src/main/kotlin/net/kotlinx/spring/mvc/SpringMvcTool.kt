package net.kotlinx.spring.mvc

import mu.KotlinLogging
import net.kotlinx.domain.menu.MenuMethod
import net.kotlinx.exception.toSimpleString
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method


/**
 * 스프링용 MVC 매핑 데이터 저장
 * rest를 사용하기도 함으로 url을 변형하지 않고 그냥 다 보여준다.
 * 프로젝트마다 변형해서 사용할것
 * */
class SpringMvcTool(val handlerMapping: RequestMappingHandlerMapping) {

    private val log = KotlinLogging.logger {}

    /** 메뉴 메소드로 변환 */
    fun toMenuMethods(): List<MenuMethod> {
        return handlerMapping.handlerMethods.entries.flatMap { (req, value) ->
            if (value.beanType.simpleName == "BasicErrorController") return@flatMap emptyList() //기본 설정 제거. 제거 방법이 없는듯?
            if (log.isTraceEnabled) {
                log.debug { "req : $req" }
                log.debug { " -> req.name : ${req.name}" }
                log.debug { " -> req.directPaths : ${req.directPaths}" }
                log.debug { " -> req.paramsCondition : ${req.paramsCondition}" }
                log.debug { " -> req.patternsCondition : ${req.patternsCondition}" }
                log.debug { " -> req.patternValues : ${req.patternValues}" }
            }
            //일단 directPaths 만 가져옴
            if (req.directPaths.isEmpty()) {
                log.warn { "경고!!  directPaths is required : $req" }
            }
            req.directPaths.map {
                MenuMethod(
                    it,
                    value.beanType,
                    value.method,
                )
            }
        }
    }

    /** 현재 컨트롤러의 매소드를 리턴 */
    fun getCurrentControllerMethod(): Method? {
        val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes? ?: return null
        try {
            val request = attributes.request
            val handlerMethod = handlerMapping.getHandler(request)?.handler as HandlerMethod?
            return handlerMethod?.method
        } catch (e: Exception) {
            log.trace { "가져오기 실패! ${e.toSimpleString()}" }
            return null
        }
    }

}
