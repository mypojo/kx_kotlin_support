package net.kotlinx.kotest.modules

import mu.KotlinLogging
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.domain.item.tempData.TempData
import net.kotlinx.domain.item.tempData.TempDataConverter
import net.kotlinx.koin.KoinModule
import net.kotlinx.kotest.MyEnv
import net.kotlinx.reflect.name
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module


object DbItemModule : KoinModule {

    private val log = KotlinLogging.logger {}

    override fun moduleConfig(): Module = module {

        run {
            val currentableName = "system-${MyEnv.SUFFIX}"
            single(named(TempData::class.name())) {
                DbTable {
                    converter = TempDataConverter(this)
                    tableName = currentableName
                }
            }
            //repository는 profile 때문에 인젝션 하지 않음
        }

    }

}
