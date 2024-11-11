package net.kotlinx.kotest.modules

import mu.KotlinLogging
import net.kotlinx.domain.ddb.DbMultiIndexItem
import net.kotlinx.domain.ddb.DbMultiIndexItemRepository
import net.kotlinx.domain.ddb.DbMultiIndexItemUtil
import net.kotlinx.domain.ddb.DdbBasicRepository
import net.kotlinx.domain.ddb.errorLog.ErrorLog
import net.kotlinx.domain.ddb.errorLog.ErrorLogConverter
import net.kotlinx.domain.ddb.repeatTask.RepeatTask
import net.kotlinx.domain.ddb.repeatTask.RepeatTaskConverter
import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins.koin
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
                tableName = "system-dev"
            }
        }

        single { DbMultiIndexItemRepository(null) }

        //==================================================== 개별 객체 ======================================================

        single(named(ErrorLog::class.name())) {
            DdbBasicRepository(
                koin<DbMultiIndexItemRepository>(),
                ErrorLogConverter(),
            )
        }

        single(named(RepeatTask::class.name())) {
            DdbBasicRepository(
                koin<DbMultiIndexItemRepository>(),
                RepeatTaskConverter(),
            )
        }


    }

}
