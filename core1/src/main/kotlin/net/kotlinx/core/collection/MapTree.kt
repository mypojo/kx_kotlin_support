package net.kotlinx.core.collection

import java.util.concurrent.atomic.AtomicLong
import java.util.function.Supplier

/**
 * 스래드 세이프한, value가 없으면 생성해주는 맵
 * 주의!!!  value에는 가변 객체가 와야 수정이 가능하다. supplier로 제공된 래퍼런스 교체는 불가능함
 * ex) value가 Long 같은 프리미티브가 아니라  MutableLong을 사용
 * 주의!!!  JSON 과는 다르게 뎁스에 스키마가 있다. 즉 트리구조가 아닌 플랫 구조이다
 *
 * 단순 계산이라면 delegate.computeIfPresent 를 사용해도 됨
 */
class MapTree<T>(
    val delegate: MutableMap<String, T> = mutableMapOf(),
    private val supplier: Supplier<T>,
) {

    //============================== method =================================
    /** 핵심 메소드  */
    @Synchronized
    operator fun get(key: Any): T {
        val keyStr = key.toString()
        return delegate[keyStr] ?: supplier.get().also { delegate[keyStr] = it }
    }

    companion object {
        /** 숫자 세기 */
        fun atomicLong(): MapTree<AtomicLong> = MapTree { AtomicLong() }

        /** 숫자 세기 - 2단 */
        fun atomicLong2(): MapTree<MapTree<AtomicLong>> = MapTree { MapTree { AtomicLong() } }
    }

}


