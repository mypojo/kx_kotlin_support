package net.kotlinx.lazyLoad

import net.kotlinx.core.ProtocolPrefix


/**
 * 간단 초기화
 * 그냥 일반 문자열에 붙여도 괜찮음!
 *  */
fun lazyLoadString(initValue: String? = null, profile: String? = null): LazyLoadStringProperty = LazyLoadStringProperty(initValue, profile)

/** AWS 파라메터 스토어로부터 로드 */
fun lazyLoadStringSsm(initValue: String, profile: String? = null): LazyLoadStringProperty = lazyLoadString("${ProtocolPrefix.SSM}${initValue}", profile)