package net.kotlinx.collection

/** 
 * range 에서 데이터를 조회할때 사용
 * list의 가장 위에 데이터가 우선함
 * 보통 년월일 등의 데이터를 사용
 * */
class RangeMap<T>(
    private val configs: List<Pair<ClosedRange<String>, T>>
) {
    /**
     * @return 매칭이 안되면 null을 리턴함
     *  */
    operator fun get(value: String): T? = configs.firstOrNull { value in it.first }?.second
}