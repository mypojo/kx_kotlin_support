package net.kotlinx.lazyLoad

import kotlin.reflect.KProperty


/**
 * 설정객체 등에서, 초기값을 동적으로 세팅하기 위한 프로퍼티
 * ex) 로지컬 네임의 경우, 할당하면 해당값 사용, 할당이 없으면 name과 환경변수 기준으로 생성
 * */
class LazyDefaultProperty<T>(
    /** 초기값 생성자 */
    private val defaultFactory: () -> T,
) {

    /** 설정된 값을 바탕으로 가져온 실제 값 */
    private var resultValue: T? = null

    /**
     * 값을 가져올때 null인경우 디폴트값을 생성해준다
     * 주의!! 디폴트값은 SSM 등에서 가져오는 경우도 많기 때문에 캐싱 해준다.
     * 매번 defaultFactory를 실행하지 않음
     * */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (resultValue == null) resultValue = defaultFactory()
        return resultValue!!
    }

    /** 재할당 해서 초기화 가능하다. */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this.resultValue = value
    }

}