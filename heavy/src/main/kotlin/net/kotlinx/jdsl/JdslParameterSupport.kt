package net.kotlinx.jdsl

import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Path
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Paths
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicate
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicates
import kotlin.reflect.KProperty1


//==================================================== JdslParameter DSL 지원 ======================================================
infix fun <T : Any> Path<T>.eq(param: JdslParameter): Predicate = param.eq(this)
infix fun <T : Any> KProperty1<T, *>.eq(param: JdslParameter): Predicate = Paths.path(this) eq param

infix fun <T : Any> Path<T>.`in`(param: JdslParameter): Predicate = param.`in`(this)
infix fun <T : Any> KProperty1<T, *>.`in`(param: JdslParameter): Predicate = Paths.path(this) `in` param

infix fun Path<String>.like(param: JdslParameter): Predicate = param.like(this)
infix fun <T : Any> KProperty1<T, String?>.like(param: JdslParameter): Predicate = Paths.path(this) like param

//==================================================== 편의 도구 ======================================================

/** enum 등의 in 표현 */
infix fun <T : Any> KProperty1<T, *>.`in`(param: List<Any>): Predicate = Predicates.`in`(Paths.path(this).toExpression(), param.map { Expressions.value(it) })