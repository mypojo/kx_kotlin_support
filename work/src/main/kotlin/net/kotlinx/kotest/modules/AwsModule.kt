package net.kotlinx.kotest.modules

import mu.KotlinLogging
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.javaSdkv2.AwsJavaSdkV2Client
import net.kotlinx.aws.toAwsClient
import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins.koin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** 해당 패키지의 기본적인 의존성 주입 */
object AwsModule : KoinModule {

    private val log = KotlinLogging.logger {}

    override fun moduleConfig(): Module = module {

        single { get<AwsConfig>().toAwsClient() }

        Aws1Module.IAM_PROFILES.profiles.forEach { pair ->
            val profile = pair.first
            single(named(profile)) {
                log.debug { "[${profile}] AwsClient 생성.." }
                koin<AwsConfig>(profile).toAwsClient()
            }
            single(named(profile)) {
                log.debug { "[${profile}] AwsJavaSdkV2Client 생성.." }
                AwsJavaSdkV2Client(koin<AwsConfig>(profile))
            }
        }
    }

}