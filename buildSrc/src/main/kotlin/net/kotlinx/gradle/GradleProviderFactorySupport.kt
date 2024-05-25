package net.kotlinx.gradle

import org.gradle.api.provider.ProviderFactory

/**
 * 그래들 표준 문법을 간단하게 변경해줌
 * ex) providers["group"]
 * 값이 없으면 더미 데이터로 채워준다 (오류 방지)
 *  */
operator fun ProviderFactory.get(name: String): String = this.gradleProperty(name).getOrElse("$name is not found")