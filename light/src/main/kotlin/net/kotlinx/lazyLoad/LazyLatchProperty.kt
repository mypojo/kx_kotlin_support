package net.kotlinx.lazyLoad

import kotlin.reflect.KProperty


/**
 * 한번만 초기화되는 프로퍼티
 * */
class LazyLatchProperty {

    /** 설정된 값을 바탕으로 가져온 실제 값 */
    private var resultValue: String? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = resultValue ?: throw IllegalStateException("Property 값이 없습니다")

    /** 재할당 해서 초기화 가능하다. */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        resultValue?.let {
            if (resultValue != value)
                throw IllegalStateException("프로퍼티 값을 을 두번 지정($resultValue -> ${value})하시면 안됩니다~")
        }
        this.resultValue = value
    }

}

