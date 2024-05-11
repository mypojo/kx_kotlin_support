package net.kotlinx.collection

/**
 * 미리 정렬된 리스트에서 주어진 숫자보다 작은것의 수 리턴 (검색에 최적화됨)
 * listOf(1,3,5,7,9).findRank(5) => 2 리턴
 * 참고로 NavMap의 경우 중복 순위를 알 수 없다.
 *  */
fun List<Int>.findRank(data: Int): Int {
    var left = 0
    var right = this.size - 1
    while (left <= right) {
        val mid = (left + right) / 2
        if (this[mid] < data) left = mid + 1 else right = mid - 1
    }
    return left
}