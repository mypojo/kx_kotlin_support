package net.kotlinx.jdsl

import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expression
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Path
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicate
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicates
import net.kotlinx.reflect.Bean
import net.kotlinx.reflect.name
import kotlin.reflect.KCallable


/**
 * kotlinjdsl 도우미
 * 대부분의 입력폼에서 null 입력시 "전체" 를 적용하기 때문에 해당 로직을 위해서 만듬
 * null 입력시 해당값 전체를 적용
 *
 * Property 대신 path 기준으로 작성함 ( camp.user.name 이런식으로 3depth 인 경우도 있음)
 *
 * 여기 없는거는 커스텀하게 만들면됨 (원래 로직에서 let 구문으로 null 리턴하면 알아서 조건 제거해줌)
 *
 * 향후 넣을거
 * 1. enum
 *
 * */
class JdslParameter(private val bean: Bean) {

    private val jpql = Jpql.newInstance()

    //==================================================== public ======================================================

    fun <T : Any> eq(path: Path<T>): Predicate? = doText(path) { path, value -> Predicates.equal(path, value) }
    fun <T : Any> notEq(path: Path<T>): Predicate? = doText(path) { path, value -> Predicates.notEqual(path, value) }

    /** 해당 값이 있으면 IN 필터 적용, 아니면 전체값 적용 */
    fun <T : Any> `in`(path: Path<T>): Predicate? {
        val pathName = path.name
        checkCommon(pathName)

        val value = bean[pathName] ?: return null
        check(value is Collection<*>) { "Collection 타입만 가능합니다" }
        if (value.isEmpty()) return null

        return Predicates.`in`(path.toExpression(), value.map { Expressions.value(it) })
    }

    /** 일반 like */
    fun like(path: Path<String>): Predicate? = doLike(path) { "%${it}%" }

    /** 뒷 like */
    fun likeStartsWith(path: Path<String>): Predicate? = doLike(path) { "${it}%" }

    /** 간단 위임 */
    override fun toString(): String = "${this::class.name()} ${bean.data}"

    //==================================================== private ======================================================

    /** 내부참조라서, 리플렉션으로 가져옴. */
    private val Path<*>.name: String
        get() {
            val prop = Bean(this)["property"] as KCallable<*>
            return prop.name
        }

    /**
     * 해당 값(text)이 있으면 EQ 필터 적용, 아니면 전체값 적용
     *  */
    private fun <T : Any> doText(path: Path<T>, block: (path: Expression<T>, value: Expression<String>) -> Predicate): Predicate? {
        val pathName = path.name
        checkCommon(pathName)

        val value = bean[pathName] ?: return null
        val text = value.toString()
        if (text.isEmpty()) return null

        return block(path.toExpression(), Expressions.value(text))
    }

    /** like 구문을 리턴해준다. */
    private fun doLike(path: Path<String>, template: (String) -> String): Predicate? {
        val pathName = path.name
        checkCommon(pathName)
        val value = bean[pathName] ?: return null
        check(value is String) { "like 의 value 값은 String 이어야 합니다." }
        val text = value.toString()
        if (text.isEmpty()) return null

        val escaped = template(escapeLike(text))
        return Predicates.like(path.toExpression(), Expressions.value(escaped))
    }

    /** 공통 체크 */
    private fun checkCommon(name: String) {
        check(bean.checkProp(name)) { "Entity와 동일한 이름의 property가 존재해야 합니다 : $name" }
    }

    companion object {

//        /** null 회피하기 위한 1=1 조건 */
//        private val OK_ANY = Predicates.equal(Expressions.value(1), Expressions.value(1))

        /**
         * 간단 생성자
         * @param bean 사용자 입력데이터 ex) dto
         *  */
        fun from(bean: Any): JdslParameter = JdslParameter(Bean(bean))

        /**
         * kotlin jdsl 에서는 자동으로 이스케이퍼 미지정시 자동으로 \ 를 이스케이핑 지정해준다.
         * ex) replace(?,'\\','\\\\')
         *  */
        fun escapeLike(text: String): String {
            val regex = Regex("[%_]")
            return regex.replace(text) { match ->
                when (match.value) {
                    "%" -> "\\%"
                    "_" -> "\\_"
                    else -> throw IllegalArgumentException("Unexpected character: ${match.value}")
                }
            }
        }
    }

}