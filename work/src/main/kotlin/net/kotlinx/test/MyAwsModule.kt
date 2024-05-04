package net.kotlinx.test

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.koin.KoinModule
import org.koin.core.module.Module
import org.koin.dsl.module

/** 해당 패키지의 기본적인 의존성 주입 */
object MyAwsModule : KoinModule {

    override fun moduleConfig(): Module = module {

        single { get<AwsConfig>().toAwsClient() }
    }

}