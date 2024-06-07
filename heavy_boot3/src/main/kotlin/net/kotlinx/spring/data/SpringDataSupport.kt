package net.kotlinx.spring.data

import org.springframework.data.domain.Page

/**
 * null safe 하게 사용
 * */
fun <T> Page<T>.content(): List<T> = this.content.filterNotNull()