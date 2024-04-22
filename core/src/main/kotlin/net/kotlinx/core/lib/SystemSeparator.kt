package net.kotlinx.core.lib


/**
 * 시스템을 구분짓는거 대충 정리
 * */
object SystemSeparator {

    /**
     * 그래들인지?
     * 로컬에서 직접 실행하는 테스트와 비교하기 위함
     *  */
    val IS_GRADLE: Boolean = SystemUtil.JVM_PARAMS.any { it.startsWith("-Dorg.gradle.native") }


}