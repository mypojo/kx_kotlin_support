package net.kotlinx.aws.ssm

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.reflect.KProperty


/**
 * /로 시작하는경우 파라메터 스토어로 간주하고 로드함
 * 별도 동기화 하지 않음
 * */
class LazySsmProperty : KoinComponent {

    /** SSM 입력 */
    lateinit var value: String

    /** SSM 결과 */
    private var ssmValue: String? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {

        if (ssmValue == null) {
            val aws = get<AwsClient1>()
            ssmValue = when {
                /** 요건 표준 AWS 접미어 */
                value.startsWith("resolve:ssm:") -> {
                    val ssmUrl = value.removePrefix("resolve:ssm:")
                    try {
                        log.debug { "SSM [$ssmUrl] 데이터 로드..." }
                        aws.ssmStore[ssmUrl]!!
                    } catch (e: Exception) {
                        throw IllegalArgumentException("ssmStore $ssmUrl not found")
                    }
                }

                /** 이거는 편의상 이렇게.. */
                value.startsWith("/") -> {
                    try {
                        log.debug { "SSM [$value] 데이터 로드..." }
                        aws.ssmStore[value]!!
                    } catch (e: Exception) {
                        throw IllegalArgumentException("ssmStore $value not found")
                    }
                }

                else -> value
            }
        }

        return ssmValue!!
    }

    /** 재할당 해서 초기화 가능하다. */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        this.value = value
        this.ssmValue = null
    }

    companion object {

        private val log = KotlinLogging.logger {}

    }

}

/**
 * SSM 파라메터 이용
 *  */
fun lazySsm(): LazySsmProperty = LazySsmProperty()