package net.kotlinx.core.number

import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import java.math.RoundingMode

/**
 * range 구간을 분리해주는 도구
 * ex) 1억개의 데이터를 20개씩 3분할 하고싶음
 *  */
class RangeSpliter {

    @Kdsl
    constructor(block: RangeSpliter.() -> Unit = {}) {
        apply(block)
    }

    /** 최대~최소 구간 정의 */
    lateinit var minmax: LongRange

    /**
     * 순환 수. 일반적으로 한번만 수행함
     * 이게 커지면 리턴 데이터 양이 증가함
     *  */
    var cycleCnt = 1

    /** 전체 스텝 수 (필수) */
    var stepCnt: Long = 0

    /**
     * 전체 스탭에 대해서 엔빵한 레인지 범위를 돌려준다.
     * 애매하게 남으면 마지막 데이터가 좀 줄어들 수 있음
     *  */
    operator fun get(index: Int): LongRange {

        check(stepCnt > 0)
        check(index < stepCnt) { "index 는 stepCnt 보다 작아야 합니다." }

        val dataSize = minmax.last - minmax.first

        val hashSize = stepCnt / cycleCnt
        val stepSize = dataSize.divide2(hashSize, 0, RoundingMode.CEILING).toLong() //올림함. 스텝 한번에 몇개의 데이터가 있을지?

        log.trace { "벨리데이션 체크" }
        val stepCycle = index / hashSize
        if (stepCycle >= cycleCnt) {
            log.debug { "stepCycle($stepCycle) 이 ($cycleCnt)을 같거나 커서 빈값을 리턴합니다" }
            return 0..0L
        }
        val stepIndex = index % hashSize
        val startStep = stepSize * stepIndex
        val endStep = (stepSize * (stepIndex + 1)) - 1
        return minmax.first + startStep..(minmax.first + endStep).minWith(minmax.last) //max를 넘을 수는 없음
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}