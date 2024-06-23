package net.kotlinx.id

import java.util.concurrent.atomic.AtomicLong

/**
 * 테스트
 * 지정된 구간은 해당 ID만 사용가능
 */
class IdMockGenerator(

    /** 테스트 이름 */
    val name: String,

    /**
     * 키값의 레인지
     * ex) 101 until 200L
     *  */
    val range: LongRange,
) {

    /** 실제 ID  */
    private val source = AtomicLong(range.first)

    /**
     * 키값을 가져온다.
     * */
    fun nextval(): Long {
        val value = source.getAndIncrement()
        if (value !in range) throw IllegalStateException("[${name}] nextval값 $value 는 채번가능한 구간(${range})을 초과했습니다.")
        return value
    }

    fun nextvalAsString(): String {
        return nextval().toString()
    }

}