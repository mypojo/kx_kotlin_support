package net.kotlinx.core1.threadlocal

import java.io.File

/**
 * 미리정해진 규칙에 따른 테스트 등을 돕는 간이도구
 * 모든 리소스는 이것으로 통제
 */
object ResourceHolder {

    /**
     * 종료되면 호출될 작업들 추가
     * ex) DB 커넥션 등의 리소스 정리
     */
    val afters: MutableList<() -> Unit> = mutableListOf()

    @Synchronized
    fun finish() {
        afters.forEach { it.invoke() }
    }

    /** close 되야하는거 여기 등록  */
    fun addResource(closeable: AutoCloseable) {
        /* 해제는. 가장 마지막에 작동해야함 */
        afters.add { closeable.close() }
    }

    /** 주로 사용된느 워크스페이스  */
    fun getWorkspace(): File? {
        val localWorkspace: String = System.getenv().getOrDefault("LOCAL_WORKSPACE", "D:/DATA/WORK")
        val dir = File(localWorkspace)
        dir.mkdirs()
        return dir
    }


}