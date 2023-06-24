package net.kotlinx.core

/**
 * 필드의 한글명 하드코딩용
 * 디폴트 변경이름이 애해하거나, 일회성 데이터일 경우 하드코딩이 필요한 구간이 있다.
 * 어노테이션이 많을 경우를 대비해서 하나로 쓰기 위해 만들었음
 * ex) @property:Comment("그룹명2")
 */
@Target(AnnotationTarget.FIELD,AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Comment(val value: String)