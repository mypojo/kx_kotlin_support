package net.kotlinx.domain.item.tempData

import net.kotlinx.aws.dynamo.enhanced.DbRepository
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.reflect.name

/**
 * DDB 간단접근용 헬퍼
 */
class TempDataRepository(profile: String? = null) : DbRepository<TempData>(profile) {

    override val dbTable by koinLazy<DbTable>(TempData::class.name())

}