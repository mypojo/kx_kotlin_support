package net.kotlinx.lazyLoad

import kotlin.reflect.KProperty


/**
 * 한번만 초기화되는 프로퍼티
 * 런타임에 할당 가능하지만, 에러 회피를 위해서 두번 할당시 강제 에러를 던짐
 * 보통 레거시 업무 마이그레이션에 사용함
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

