package net.kotlinx.domain.ddb

import net.kotlinx.core.DataConverter


/**
 * DbMultiIndexItem 을 실제 T 로 변경해주는 컨버터
 * */
interface DbMultiIndexEachConverter<multiIndexItem : DbMultiIndexItem, T> : DataConverter<multiIndexItem, T> {

    val pkPrefix: String
    val skPrefix: String

}