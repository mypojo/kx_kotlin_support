package net.kotlinx.reflect

import mu.KotlinLogging
import net.kotlinx.string.TextGrid
import net.kotlinx.string.toTextGrid
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

/**
 * 데이터를 가져오고 수정하는 간단 리플렉션 빈
 * */
class Bean(val data: Any) {

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
    fun get(prp: KProperty<*>): Any? = prp.getter.call(data)

    fun put(name: String, value: Any?) {
        mutableProps[name]?.setter?.call(data, value)
    }

    /** 해당 이름의 프로퍼티가 존재하는지? */
    fun checkProp(name: String): Boolean = props[name] != null

    //==================================================== 간단출력 ======================================================

    /**
     * 순서를 가진 키 리스트
     * 데이터클래스 && 생성자 있으면 넣어주고, 아니면 그냥 쓴다.
     *  */
    val ordered: List<String> by lazy {
        if (clazz.isData) {
            val constructor = clazz.constructors.maxBy { it.parameters.size }
            constructor.parameters.map { it.name!! }
        } else {
            props.keys.toList()
        }
    }

    /** 단건 그리드 변환 */
    fun toTextGrid(): TextGrid = ordered.toTextGrid(listOf(toArray()))

    /** 리스트 변환 */
    fun toList(): List<Any?> = ordered.map { props[it]!!.getter.call(data) }

    /** 라인으로 변환 */
    fun toLine(): List<String> = toList().map { it?.toString() ?: "" }

    /** 어레이로 변환 (편의용도) */
    fun toArray(): Array<Any?> = toList().toTypedArray<Any?>()

    /** 헤더값 추출 */
    fun toHeader(): List<String> = ordered.toList()

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
         * 생성자와 라인의 순서가 완전히 일치하는게 있어야 파싱이됨
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

        /**
         * CSV 등에서 객체를 생성할때 사용함 (유연 매핑 버전)
         * - 생성자 인자 수와 라인 수가 정확히 일치하지 않아도 됨
         * - 모자란 값은 공백 문자열("")로 채워넣음
         * - 라인이 더 길면 초과분은 무시함
         * - 대상 타입은 단순 DTO를 가정
         */
        fun <T : Any> fromLineIgnore(to: KClass<T>, lines: List<String>): T {
            try {
                // 가장 파라미터가 많은 생성자를 선택 (일반적으로 데이터 클래스의 주 생성자)
                val constructor = to.constructors.maxBy { it.parameters.size }
                val args = constructor.parameters.mapIndexed { index, param ->
                    val raw = lines.getOrNull(index) ?: ""
                    param.type.from(raw)
                }
                return constructor.call(*args.toTypedArray())
            } catch (e: Exception) {
                log.warn { "파싱 실패(fromLineIgnore)!! $to from $lines" }
                throw e
            }
        }


    }


}