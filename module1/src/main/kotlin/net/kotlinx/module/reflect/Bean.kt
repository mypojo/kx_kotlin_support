package net.kotlinx.module.reflect

import net.kotlinx.core.string.TextGrid
import net.kotlinx.core.string.toTextGrid
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

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

    fun put(name: String, value: Any?) {
        mutableProps[name]?.setter?.call(data, value)
    }

    fun toTextGrid(): TextGrid {
        val datas = props.values.map { it.getter.call(data) }.toTypedArray()
        return props.keys.toList().toTextGrid(listOf(datas))
    }

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

        /**
         * CSV 등에서 객체를 생성할때 사용함
         * 수신 객체는 단순한 dto 여야 함
         *  */
        fun <T : Any> fromLine(to: KClass<T>, lines: List<String>): T {
            val constructor = to.constructors.firstOrNull { it.parameters.size == lines.size }
            checkNotNull(constructor) { "클래스 [${to.simpleName}] 에 입력인자 길이 ${lines.size} 와 일치하는 생성자가 존재하지 않습니다. " }

            val args = constructor.parameters.mapIndexed { index, param -> param.type.from(lines[index]) }
            return constructor.call(*args.toTypedArray())
        }

    }


}