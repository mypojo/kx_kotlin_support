package net.kotlinx.domain.jpa


import org.hibernate.Hibernate


/** hibernate 늦은 객체 초기화 */
fun <ID, T : EntityWithId<ID>> T.load(): T = Hibernate.initialize(this) as T

/** hibernate 늦은 객체 초기화 */
fun <ID, T : EntityWithId<ID>> List<T>.load() = Hibernate.initialize(this)

/**
 * 실제 내가 만든 객체로 프록시를 벗겨줌.
 * */
fun <ID, T : EntityWithId<ID>> T.unproxy(): T = Hibernate.unproxy(this) as T