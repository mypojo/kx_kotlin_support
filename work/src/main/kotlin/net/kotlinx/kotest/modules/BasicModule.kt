package net.kotlinx.kotest.modules

import com.google.common.eventbus.DeadEvent
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.google.common.eventbus.SubscriberExceptionHandler
import mu.KotlinLogging
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.file.slash
import net.kotlinx.google.GoogleSecret
import net.kotlinx.google.GoogleService
import net.kotlinx.google.calendar.GoogleCalendar
import net.kotlinx.id.IdGenerator
import net.kotlinx.json.gson.GsonSet
import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.MyEnv
import net.kotlinx.kotest.modules.job.JobEvenListener
import net.kotlinx.kotest.modules.lambdaDispatcher.AwsEventBridgeListener
import net.kotlinx.kotest.modules.lambdaDispatcher.AwsEventListener
import net.kotlinx.kotest.modules.lambdaDispatcher.AwsSnsListener
import net.kotlinx.kotest.modules.lambdaDispatcher.LambdaDispatcherDefaultListener
import net.kotlinx.lazyLoad.lazyLoad
import net.kotlinx.lazyLoad.lazyLoadSsm
import net.kotlinx.lazyLoad.lazyLoadStringSsm
import net.kotlinx.notion.NotionDatabaseClient
import net.kotlinx.notion.NotionPageBlockClient
import net.kotlinx.reflect.name
import net.kotlinx.slack.SlackApp
import net.kotlinx.slack.SlackMessageSenders
import okhttp3.OkHttpClient
import org.jraf.klibnotion.client.Authentication
import org.jraf.klibnotion.client.ClientConfiguration
import org.jraf.klibnotion.client.NotionClient
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicLong

/** 해당 패키지의 기본적인 의존성 주입 */
object BasicModule : KoinModule {

    private val log = KotlinLogging.logger {}

    override fun moduleConfig(): Module = module {

        //==================================================== 기본 ======================================================
        single { OkHttpClient() }
        single {
            val token by lazyLoadStringSsm("/slack/token")
            SlackApp(token)
        }
        single { GsonSet.GSON }
        /** 이벤트버스 구독은 리플렉션 하지말고 개별 모듈에서 명시적으로 등록하자. */
        single {

            class DeadEventListener {
                @Subscribe
                fun onEvent(event: DeadEvent) {
                    SlackMessageSenders.Alert.send {
                        workDiv = DeadEvent::class.name()
                        descriptions = listOf("Guava 데드 메세지 발생!!")
                        body = listOf(event.event.toString())
                    }
                }
            }

            val exceptionHandler = SubscriberExceptionHandler { e, context ->
                log.error { "Guava Event 처리중 예외!! ${context.event}" }
                e.printStackTrace()
                if (!MyEnv.IS_LOCAL) {
                    SlackMessageSenders.Alert.send {
                        workDiv = SubscriberExceptionHandler::class.name()
                        descriptions = listOf("Guava Event 처리중 예외")
                        body = listOf(context.event.toString())
                    }
                }
            }

            EventBus(exceptionHandler).apply {
                register(DeadEventListener())
                register(JobEvenListener())
                register(LambdaDispatcherDefaultListener())
                register(AwsEventBridgeListener())
                register(AwsEventListener())
                register(AwsSnsListener())
            }
        }
        single {
            val tempSeq = AtomicLong()
            IdGenerator({ tempSeq.incrementAndGet() })
        }

        //==================================================== 각종 오픈 API  ======================================================

        /** 오픈소스 노션 SDK */
        single {
            val secretValue by lazyLoadStringSsm("/notion/key")
            NotionClient.newInstance(
                ClientConfiguration(
                    Authentication(secretValue)
                )
            )
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
            log.info { "구글 서비스가 로드됩니다" }
            val secret = GoogleSecret {
                secretClientFile.lazyLoadSsm("/google/app-access/oauth2_client")
                secretDir.slash(GoogleSecret.SECRET_STORED_FILE_NAME).lazyLoad(S3Data("kotlinx", "store/secret/google/app-access/StoredCredential")).load()
            }
            secret.createService()
        }

        single {
            val googleService by koinLazy<GoogleService>()
            GoogleCalendar(googleService)
        }

    }

}