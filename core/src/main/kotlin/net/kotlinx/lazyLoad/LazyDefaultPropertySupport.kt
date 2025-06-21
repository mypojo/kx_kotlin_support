package net.kotlinx.lazyLoad


/** 기본설정 할당 */
fun <T> default(defaultFactory: () -> T): LazyDefaultProperty<T> = LazyDefaultProperty(defaultFactory)