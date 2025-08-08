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
