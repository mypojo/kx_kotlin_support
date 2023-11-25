package net.kotlinx.aws.ssm

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsClient1
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.reflect.KProperty


/**
 * /로 시작하는경우 파라메터 스토어로 간주하고 로드함
 * */
class LazySsmProperty : KoinComponent {

    lateinit var value: String

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {

        return when {
            // AWS 파라메터 스토어로 간주
            value.startsWith("/") -> {
                runBlocking {
                    val aws = get<AwsClient1>()
                    try {
                        return@runBlocking aws.ssmStore[value]!!
                    } catch (e: Exception) {
                        throw IllegalArgumentException("ssmStore $value not found")
                    }
                }
            }

            else -> value
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        this.value = value
    }
}

/**
 * SSM 파라메터 이용
 *  */
inline fun lazySsm(): LazySsmProperty = LazySsmProperty()