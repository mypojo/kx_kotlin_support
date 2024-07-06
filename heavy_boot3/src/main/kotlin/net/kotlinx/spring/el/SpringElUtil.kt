package net.kotlinx.spring.el

import org.springframework.expression.ExpressionParser
import org.springframework.expression.common.TemplateParserContext
import org.springframework.expression.spel.standard.SpelExpressionParser

/**
 * 스프링 EL
 * ex) 이미 완성된 문구와 객체를 조합해서 텍스트를 만들어내고 싶은 경우
 * 각종 수식 지원 (테스트 참고)
 */
object SpringElUtil {

    /** 스래드 세이프한듯. 별 문제 없어서 그냥 같이 쓴다 */
    private val EXPRESSION_PARSER: ExpressionParser = SpelExpressionParser()

    /** 간단 템플릿 */
    fun elFormat(pattern: String): String = EXPRESSION_PARSER.parseExpression(pattern, TemplateParserContext()).getValue(String::class.java)!!

    /**
     * http://docs.spring.io/spring/docs/3.0.x/reference/expressions.html
     * EL 표현식으로 템플릿 문자열을 생성한다.
     * 다양한 활용이 가능하다. 간단한 구문에 사용하자
     * ex) random number is #{\[vo\].rowSize} *
     * 단위테스트 참고할것
     *
     * TemplateParserContext 는 디폴트 쓴다. $ 로 하면 기본이랑 헷갈림
     *
     * map 사용시 별도의 래퍼가 필요함   ex) #{map['accId']}
     *
     * 근데 json이 안됨..
     */
    fun elFormat(templateText: String, bean: Any): String = EXPRESSION_PARSER.parseExpression(templateText, TemplateParserContext()).getValue(bean, String::class.java)!!

    /** 객체에서 해당 값을 추출  */
    fun <T> extract(express: String, bean: Any): T {
        val exp = EXPRESSION_PARSER.parseExpression(express)
        return exp.getValue(bean) as T
    }
}
