package net.kotlinx.aws.dynamo.multiIndex

import net.kotlinx.aws.dynamo.enhanced.DbTable

object DbMultiIndexItemUtil {

    fun createDefault(block: DbTable.() -> Unit = {}) = DbTable {
        converter = DbMultiIndexConverter(this)
        block()
    }


}