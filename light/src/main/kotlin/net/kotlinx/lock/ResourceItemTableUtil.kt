package net.kotlinx.lock

import net.kotlinx.aws.ddb.DbTable

object ResourceItemTableUtil {

    fun createDefault(block: DbTable.() -> Unit = {}) = DbTable {
        converter = ResourceItemConverter(this)
        block()
    }


}