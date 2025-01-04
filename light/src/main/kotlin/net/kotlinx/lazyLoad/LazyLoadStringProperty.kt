package net.kotlinx.lazyLoad

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.ssm.ssmStore
import net.kotlinx.core.ProtocolPrefix
import net.kotlinx.exception.toSimpleString
import net.kotlinx.koin.Koins
import kotlin.reflect.KProperty


/**
 * 설정 객체 등에서 사용하는 통합 데이터 로드기
 * 별도 JVM 동기화 하지 않음
 *
 * 일반 lazy의 리셋 가능한 버전은 생략.. (니즈가 없고 복잡함)
 * */
class LazyLoadStringProperty(
    /**
     * 설정된 값.
     * 런타임 수정도 가능하다.
     * */
    private var initValue: String? = null,
    /** 프로파일 정보 */
    private val profile: String? = null,
) {

    /** 설정된 값을 바탕으로 가져온 실제 값 */
    private var resultValue: String? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        checkNotNull(initValue)
        synchronized(this) {
            if (resultValue == null) {
                val aws = Koins.koin<AwsClient>(profile)
                resultValue = when {

                    /** 요건 표준 AWS 접미어 */
                    initValue!!.startsWith(ProtocolPrefix.SSM) -> {
                        val ssmUrl = initValue!!.removePrefix(ProtocolPrefix.SSM)
                        try {
                            aws.ssmStore[ssmUrl]
                        } catch (e: IllegalStateException) {
                            throw IllegalArgumentException("ssmStore $ssmUrl not found  -> ${e.toSimpleString()}")
                        }
                    }

                    else -> initValue
                }
            }
            return resultValue!!
        }
    }

    /** 재할당 해서 초기화 가능하다. */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        this.initValue = value
        this.resultValue = null
    }

    companion object {

        private val log = KotlinLogging.logger {}


    }

}

/**
 * 간단 초기화
 * 그냥 일반 문자열에 붙여도 괜찮음!
 *  */
fun lazyLoadString(initValue: String? = null, profile: String? = null): LazyLoadStringProperty = LazyLoadStringProperty(initValue, profile)

/** AWS 파라메터 스토어로부터 로드 */
fun lazyLoadStringSsm(initValue: String, profile: String? = null): LazyLoadStringProperty = lazyLoadString("${ProtocolPrefix.SSM}${initValue}", profile)