package net.kotlinx.guava

import com.google.common.collect.Maps

/**
 * Guava Maps 관련 유틸
 */
object GuavaMapUtil {

    /**
     * 두 map을 묶어서 간단 Pair 로 만들어주는 편의 메소드
     * N:N 조합의 경우 모든 경우의 수를 리턴해준다.
     *
     *  ex) DDB 테이블 2개 조인
     * */
    fun <ID, A, B> mapDifferenceToPair(lefts: Map<ID, List<A>>, rights: Map<ID, List<B>>): List<Pair<A, B>> {
        val difference = Maps.difference(lefts, rights)
        check(difference.entriesOnlyOnLeft().isEmpty()) { "entriesOnlyOnLeft ${difference.entriesOnlyOnLeft().size} 발견!" }
        check(difference.entriesOnlyOnRight().isEmpty()) { "entriesOnlyOnRight ${difference.entriesOnlyOnRight().size} 발견!" }
        return difference.entriesDiffering().values.flatMap {
            it.leftValue().flatMap { left ->
                it.rightValue().map { right ->
                    left as A to right as B
                }
            }
        }
    }

}
