package net.kotlinx.reflect

import mu.KotlinLogging
import net.kotlinx.core.concurrent.parallelExecute
import net.kotlinx.core.string.TextGrid
import net.kotlinx.core.string.toTextGrid
import java.util.concurrent.Callable
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty


/** CSV 등에서 간단 변환 */
inline fun <reified T : Any> List<List<String>>.fromLines(): List<T> =
    this.map { Callable { Bean.fromLine(T::class, it) } }.parallelExecute(Runtime.getRuntime().availableProcessors())

/**
 * 데이터를 가져오고 수정하는 간단 리플렉션 빈
 * */
class Bean(
    val data: Any
) {

    private val clazz: KClass<*> = data::class

    /** 많을 수 있어서 캐시함 */
    private val props: Map<String, KProperty<*>> by lazy { clazz.props().associateBy { it.name } }

    /** 많을 수 있어서 캐시함 */
    private val mutableProps: Map<String, KMutableProperty<*>> by lazy { clazz.props().filterIsInstance<KMutableProperty<*>>().associateBy { it.name } }

    val mutablePropKeys: Set<String>
        get() = mutableProps.keys

    //==================================================== 간단 리플렉션 ======================================================

    /**
     * operator에 제너릭 안됨 -> 제너릭 적용하지 않음
     * */
    operator fun get(name: String): Any? = props[name]?.getter?.call(data)

    /** 프로퍼티로부터 직접 호출 */
    fun get(prp: KProperty<*>): Any? = prp.getter?.call(data)

    fun put(name: String, value: Any?) {
        mutableProps[name]?.setter?.call(data, value)
    }

    /** 해당 이름의 프로퍼티가 존재하는지? */
    fun checkProp(name: String): Boolean = props[name] != null

    //==================================================== 간단출력 ======================================================

    /** 단건 그리드 변환 */
    fun toTextGrid(): TextGrid = props.keys.toList().toTextGrid(listOf(toArray()))

    /** 단건 어레이로 변환 */
    fun toArray(): Array<Any?> = props.values.map { it.getter.call(data) }.toTypedArray<Any?>()

    /** 헤더값 추출 */
    fun toHeader(): List<String> = props.keys.toList()

    //==================================================== 변환 ======================================================

    /** 신규객체 생성 (생성자가 있다면 해당 값 입력) */
    fun <T : Any> newInstance(to: KClass<T>): T {
        val cons = to.constructors.minBy { it.parameters.size } //가장 인자가 적은걸로 생성
        val consArgs = cons.parameters.map { get(it.name!!) }
        return cons.call(*consArgs.toTypedArray())
    }

    /** 신규 객체를 생성해서, 데이터를 복사해준다. */
    fun <T : Any> convert(to: KClass<T>): T {
        val newInstance = newInstance(to)

        val fromBean = this
        val toBean = Bean(newInstance)
        toBean.mutableProps.keys.forEach { name ->
            fromBean[name]?.let {
                toBean.put(name, it)
            }
        }
        return newInstance
    }

    companion object {

        private val log = KotlinLogging.logger {}

        /**
         * CSV 등에서 객체를 생성할때 사용함
         * 수신 객체는 단순한 dto 여야 함
         *  */
        fun <T : Any> fromLine(to: KClass<T>, lines: List<String>): T {
            try {
                val constructor = to.constructors.firstOrNull { it.parameters.size == lines.size }
                checkNotNull(constructor) { "클래스 [${to.simpleName}] 에 입력인자 길이 ${lines.size} 와 일치하는 생성자가 존재하지 않습니다. " }

                val args = constructor.parameters.mapIndexed { index, param -> param.type.from(lines[index]) }
                return constructor.call(*args.toTypedArray())
            } catch (e: Exception) {
                log.warn { "파싱 실패!! $to from $lines" }
                throw e
            }
        }


    }


}