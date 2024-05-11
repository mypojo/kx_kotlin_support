package net.kotlinx.kotest.modules

import com.google.common.eventbus.EventBus
import net.kotlinx.core.ProtocolPrefix
import net.kotlinx.id.IdGenerator
import net.kotlinx.json.gson.GsonSet
import net.kotlinx.koin.KoinModule
import net.kotlinx.lazyLoad.lazyLoadStringSsm
import net.kotlinx.notion.NotionDatabaseClient
import net.kotlinx.notion.NotionPageBlockClient
import net.kotlinx.openAi.OpenAiClient
import net.kotlinx.openAi.OpenAiModels
import net.kotlinx.slack.SlackApp
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicLong

/** 해당 패키지의 기본적인 의존성 주입 */
object MyBasicModule : KoinModule {

    override fun moduleConfig(): Module = module {

        //==================================================== 기본 ======================================================
        single { OkHttpClient() }
        single {
            val token by lazyLoadStringSsm("/slack/token")
            SlackApp(token)
        }
        single { GsonSet.GSON }
        single { EventBus() }
        single {
            val tempSeq = AtomicLong()
            IdGenerator({ tempSeq.incrementAndGet() })
        }
        single {
            val secretValue by lazyLoadStringSsm("/notion/key")
            NotionDatabaseClient(secretValue)
        }
        single {
            val secretValue by lazyLoadStringSsm("/notion/key")
            NotionPageBlockClient(secretValue)
        }
        single {
            OpenAiClient {
                apiKey = "${ProtocolPrefix.SSM}/gpt4/demo/key"
                modelId = OpenAiModels.GPT_4
            }
        }

    }

}