package net.kotlinx.system

import net.kotlinx.string.TextGrid
import net.kotlinx.string.abbr
import net.kotlinx.string.toTextGrid
import net.kotlinx.string.toTextGridPrint
import java.lang.management.ManagementFactory
import java.net.Inet4Address


object SystemUtil {

    val IP: String by lazy { Inet4Address.getLocalHost().hostAddress }

    /**
     * 시스템 프로퍼티 출력
     * 시스템 프로퍼티는 커스텀하게 수정하지 못하는거같음
     *  */
    fun systemPropertyPrint() = listOf("name", "value").toTextGridPrint { System.getProperties().map { arrayOf(it.key, it.value.toString().abbr(50)) } }

    /** 시스템 프로퍼티 가져옴 */
    fun systemValue(key: String): String? = System.getProperties()[key]?.toString()

    /**
     * 환경변수 출력
     * 윈도우 속성에 있는, path 설정하는 그거
     * 실행 옵션으로 넣을 수 있음. 런타임 수정은 권장하지 않음
     * 시크릿 키 등을 입력할때도 사용함
     *  */
    fun envPrint() = listOf("name", "value").toTextGridPrint { System.getenv().map { arrayOf(it.key, it.value.abbr(50)) } }

    /**
     * 환경변수에 값이 있으면 가져오고
     * 아니면 key를 리턴한다.
     * ex) 프로젝트 이름을 git에 노출시키지 않고 조회
     * */
    fun envValue(key: String): String = System.getenv()[key] ?: key


    /** JVM 파라메터 */
    val JVM_PARAMS: List<String> by lazy { ManagementFactory.getRuntimeMXBean().inputArguments }

    /**
     * JVM 파라메터 출력
     * 실행 옵션으로 넣을 수 있음.
     *  */
    fun jvmParamPrint() =
        listOf("name", "value").toTextGridPrint {
            JVM_PARAMS.map {
                val split = it.split("=")
                check(split.size <= 2)
                arrayOf(split[0], split.getOrNull(1)?.abbr(50) ?: "") // = 가 없을 수 있음 ex) -ea
            }
        }

    fun nowUsedMemory(): Long {
        Runtime.getRuntime().gc() //좀더 정확한 값을 알기 위해?? 측정 직전에 GC한다. => 쓸모없음 -> 그래도 일단 추가
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    }

    /** 환경변수를 간단 텍스트 그리드로 */
    fun envToTextGrid(): TextGrid = listOf("name", "value").toTextGrid(System.getenv().map { arrayOf(it.key, it.value) })


}