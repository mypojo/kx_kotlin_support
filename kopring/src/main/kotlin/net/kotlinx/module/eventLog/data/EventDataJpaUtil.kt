package net.kotlinx.module.eventLog.data

import org.hibernate.proxy.HibernateProxy
import org.springframework.data.domain.Persistable


object EventDataJpaUtil {

    /**
     * 프록시 객체를 벗겨서 문자열로 제공 (사람이 UI로 보는용)
     * 변경 전후 값 비교를 어떻에 저장할지.
     * 객체 링크가 변경된 경우 적절하게 변경해준다
     *  */
    fun entityDataToSimpleString(value: Any?): String = when (value) {
        is Persistable<*> -> {
            val clazz = if (value is HibernateProxy) value.javaClass.superclass else value.javaClass  //프록시 벗겨야함
            val entityName = clazz.simpleName.substringAfterLast(".")
            "$entityName(${value.id})"
        }

        is Collection<*> -> value.joinToString(",") { entityDataToSimpleString(it) }
        else -> value?.toString() ?: ""
    }

}