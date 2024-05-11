package net.kotlinx.id

/**
 * GUID 대용량 채번기.
 * 단순 OLTP의 경우 제공되는 시퀀스를 사용해도 되지만
 * 배치 로직이 결합된 경우를 대비해서 만들어짐
 *
 * 고객이 1부터 순서대로 증가하는 자연키 스타일을 요구할경우 사용하면 안됨
 *
 * 하이값 : 제공된 소스의 키값(오라클 시퀀스 or MYSQL 채번테이블값) 사용
 * 로우값 : JVM 락으로 제공된 하이값 당 X개의 키  제공
 */
class IdGenerator(
    /**
     * ORACLE : 시퀀스 사용
     * MYSQL : 별도 JDBC로 구현
     */
    private val source: () -> Long,
    /**
     * 하이값 1개당 10000개의 키를 사용한다.
     * 시스템 오픈 이후 수정하면 안됨!
     *  */
    private val weight:Int = 10000,
) {

    //==================================================== 내부 사용 ======================================================@
    private var currentValue: Long = 1
    private var maxValue: Long = 0

    /**
     * 키값을 가져온다.
     * 1을   로드한 경우   10001~  20000 까지의 키를 리턴한다.
     * 109를 로드한 경우 1090001~2000000 까지의 키를 리턴한다.
     * */
    @Synchronized
    fun nextval(): Long {
        if (currentValue > maxValue) {
            val seq = source.invoke()
            currentValue = seq * weight + 1
            maxValue = (seq + 1) * weight
        }
        return currentValue++
    }

    fun nextvalAsString(): String {
        return nextval().toString()
    }

}