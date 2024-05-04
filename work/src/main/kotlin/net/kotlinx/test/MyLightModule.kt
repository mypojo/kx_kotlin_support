package net.kotlinx.test

import com.google.common.eventbus.EventBus
import net.kotlinx.aws.AwsClient1
import net.kotlinx.core.gson.GsonSet
import net.kotlinx.core.id.IdGenerator
import net.kotlinx.koin.KoinModule
import net.kotlinx.notion.NotionDatabaseClient
import net.kotlinx.slack.SlackApp
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicLong

/** 해당 패키지의 기본적인 의존성 주입 */
object MyLightModule : KoinModule {

    override fun moduleConfig(): Module = module {

        //==================================================== 기본 ======================================================
        single { OkHttpClient() }
        single {
            val token = get<AwsClient1>().ssmStore["/slack/token"]!!
            SlackApp(token)
        }
        single { GsonSet.GSON }
        single { EventBus() }
        single {
            val tempSeq = AtomicLong()
            IdGenerator({ tempSeq.incrementAndGet() })
        }
        single {
            val aws: AwsClient1 by inject()
            val secretValue = aws.ssmStore["/notion/key"]!!
            NotionDatabaseClient(secretValue)
        }

    }

}