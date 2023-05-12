package net.kotlinx.kopring.spring.mvc

import mu.KotlinLogging
import net.kotlinx.core2.menu.MenuData
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping


/**
 * 스프링용 MVC 매핑 데이터 저장
 * rest를 사용하기도 함으로 url을 변형하지 않고 그냥 다 보여준다.
 * 프로젝트마다 변형해서 사용할것
 * */
object SpringMvcUtil {

    private val log = KotlinLogging.logger {}

    fun toMap(handlerMapping: RequestMappingHandlerMapping): Map<String, MenuData> {
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
                MenuData(
                    it,
                    value.beanType,
                    value.method,
                )
            }
        }.associateBy { it.url }
    }

}
