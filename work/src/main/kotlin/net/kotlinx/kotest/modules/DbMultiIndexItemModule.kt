package net.kotlinx.kotest.modules

import mu.KotlinLogging
import net.kotlinx.aws.dynamo.multiIndex.DbMultiIndexItem
import net.kotlinx.aws.dynamo.multiIndex.DbMultiIndexItemRepository
import net.kotlinx.aws.dynamo.multiIndex.DbMultiIndexItemUtil
import net.kotlinx.aws.dynamo.multiIndex.DbMultiindexItemGenericRepository
import net.kotlinx.domain.item.repeatTask.RepeatTask
import net.kotlinx.domain.item.repeatTask.RepeatTaskConverter
import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.MyEnv
import net.kotlinx.reflect.name
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module


object DbMultiIndexItemModule : KoinModule {

    private val log = KotlinLogging.logger {}

    override fun moduleConfig(): Module = module {

        //==================================================== 공통 ======================================================

        single(named(DbMultiIndexItem::class.name())) {
            DbMultiIndexItemUtil.createDefault {
                tableName = "system-${MyEnv.SUFFIX}"
            }
        }

        single { DbMultiIndexItemRepository() }

        //==================================================== 개별 객체 ======================================================

        single(named(RepeatTask::class.name())) {
            DbMultiindexItemGenericRepository(
                koin<DbMultiIndexItemRepository>(),
                RepeatTaskConverter(),
            )
        }


    }

}
