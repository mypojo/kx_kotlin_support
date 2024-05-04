package net.kotlinx.props

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins
import kotlin.reflect.KProperty


/**
 * 설정 객체 등에서 사용하는 통합 데이터 로드기
 * 별도 JVM 동기화 하지 않음
 * */
class LazyLoadProperty {

    /** 설정된 값 */
    lateinit var initValue: String

    /** 설정된 값을 바탕으로 가져온 실제 값 */
    private var resultValue: String? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {

        if (resultValue == null) {
            val aws = Koins.koin<AwsClient1>()
            resultValue = when {

                /** 요건 표준 AWS 접미어 */
                initValue.startsWith(SSM) -> {
                    val ssmUrl = initValue.removePrefix(SSM)
                    try {
                        log.debug { "SSM [$ssmUrl] 데이터 로드..." }
                        aws.ssmStore[ssmUrl]!!
                    } catch (e: Exception) {
                        throw IllegalArgumentException("ssmStore $ssmUrl not found")
                    }
                }

                else -> initValue
            }
        }

        return resultValue!!
    }

    /** 재할당 해서 초기화 가능하다. */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        this.initValue = value
        this.resultValue = null
    }

    companion object {

        private val log = KotlinLogging.logger {}

        const val SSM = "resolve:ssm:"

        fun ssm(initValue: String) = "${SSM}${initValue}"

    }

}

/**
 * 간단 초기화
 * 그냥 일반 문자열에 붙여도 괜찮음!
 *  */
fun lazyLoadString(initValue: String? = null): LazyLoadProperty = LazyLoadProperty().also { p -> initValue?.let { p.initValue = initValue } }

/** 간단 초기화 */
fun lazyLoadSsm(initValue: String): LazyLoadProperty = lazyLoadString(LazyLoadProperty.ssm(initValue))