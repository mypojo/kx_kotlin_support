package net.kotlinx.collection

/**
 * 두 Map을 비교하여 차이점을 분석하는 클래스
 * Guava의 MapDifference를 대체
 *
 * @property leftOnly left에만 있는 키와 값
 * @property rightOnly right에만 있는 키와 값
 * @property commonKeys 공통 키와 양쪽 값의 쌍 (left 값, right 값)
 */
class MapJoin<L, R>(
    left: Map<String, L>,
    right: Map<String, R>
) {

    /** left에만 있는 키와 값 */
    val leftOnly: Map<String, L> = left.filterKeys { it !in right.keys }

    /** right에만 있는 키와 값 */
    val rightOnly: Map<String, R> = right.filterKeys { it !in left.keys }

    /** 공통 키와 양쪽 값의 쌍 (left 값, right 값) */
    val commonKeys: Map<String, Pair<L, R>> = left.keys
        .intersect(right.keys)
        .associateWith { key ->
            left[key]!! to right[key]!!
        }
}