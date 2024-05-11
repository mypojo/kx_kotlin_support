package net.kotlinx.number

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 반올림
 * ex) 비율을 구하는 경우 ( A * 100.0 / B).toRate(1)
 *  */
fun BigDecimal.halfUp(scale: Int): BigDecimal = this.setScale(scale, RoundingMode.HALF_UP)

/**
 * 기본 div는 이미 있음 (코틀린)
 * 반드시 this.setScale(scale) 후 호출할것!!
 * 가능하면 이거 쓰지 말것!!!
 *  */
fun BigDecimal.div2(value: BigDecimal): BigDecimal {
    if (value == BigDecimal.ZERO) return BigDecimal.ZERO
    return this / value //이렇게 하면 kotlin div (무조건 반올림) 됨
}

/**
 * 기본 divide는 이미 있음 (자바)
 * 반드시 this.setScale(scale) 후 호출할것!!
 * @param mode 일반적인 올림 처리시 RoundingMode.CEILING 을 써야함
 *  */
fun BigDecimal.divide2(value: BigDecimal, mode: RoundingMode = RoundingMode.HALF_UP): BigDecimal {
    if (value == BigDecimal.ZERO) return BigDecimal.ZERO
    return this.divide(value, mode)
}

/** 간단메소드 Long버전.  */
fun Long.divide2(value: Long, scale: Int = 0, mode: RoundingMode = RoundingMode.HALF_UP): BigDecimal {
    return this.toBigDecimal().setScale(scale).divide2(value.toBigDecimal(), mode)
}
/** 간단메소드 Int버전. */
fun Int.divide2(value: Int, scale: Int = 0, mode: RoundingMode = RoundingMode.HALF_UP): BigDecimal {
    return this.toBigDecimal().setScale(scale).divide2(value.toBigDecimal(), mode)
}
