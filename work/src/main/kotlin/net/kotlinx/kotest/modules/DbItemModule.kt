package net.kotlinx.kotest.modules

import mu.KotlinLogging
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.domain.item.errorLog.ErrorLog
import net.kotlinx.domain.item.errorLog.ErrorLogConverter
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

            //==================================================== 이하 Repository는 profile때문에 생략 (플젝별로 만드세요) ======================================================

            /** 임시 데이터 처리 */
            single(named(TempData::class.name())) {
                DbTable {
                    converter = TempDataConverter(this)
                    tableName = currentableName
                }
            }

            /** 에러로그 */
            single(named(ErrorLog::class.name())) {
                DbTable {
                    converter = ErrorLogConverter(this)
                    tableName = currentableName
                }
            }
        }

    }

}
