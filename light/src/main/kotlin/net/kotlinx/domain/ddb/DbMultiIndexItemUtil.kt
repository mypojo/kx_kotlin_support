package net.kotlinx.domain.ddb

import net.kotlinx.aws.ddb.DbTable

object DbMultiIndexItemUtil {

    fun createDefault(block: DbTable.() -> Unit = {}) = DbTable {
        converter = DbMultiIndexConverter(this)
        block()
    }


}