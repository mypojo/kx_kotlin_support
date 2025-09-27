package net.kotlinx.core


/**
 * 특정 로직을 마킹하기 위한 용도
 * 하드코딩 방지용
 * */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SecurityMarked(val value: String)