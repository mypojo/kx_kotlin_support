package net.kotlinx.kotest

import io.kotest.core.Tag
import io.kotest.core.spec.Spec

/**
 * 런타임에 태그를 확인하는게 아직 불가능한거 같다.
 * 이때문에 일단 이렇게 임시로 사용..
 * */
fun Spec.initTest(vararg tags: Tag) {
    tags(*tags)
}

/**
 * 테스트 간단 유형.
 *
 * ex) tags(KotestUtil.FAST, KotestUtil.TESTING)
 * */
object KotestUtil {

    /** 일반적인 빠른 테스트 */
    val FAST = Tag("fast")

    /** 스래드 등의 느린 테스트 */
    val SLOW = Tag("slow")

    /** 테스트중인거 */
    val TESTING = Tag("testing")

    /**
     * 테스트 실행필요 없음
     *
     *  */
    val IGNORE = Tag("ignore")

    //==================================================== 프로젝트 관련 (비용이 발생하거나, 리소스가 필요하거나 등 ======================================================

    /** NV */
    val PROJECT01 = Tag("kx.profile01")

    /** SK */
    val PROJECT02 = Tag("kx.profile02")

}