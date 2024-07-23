package net.kotlinx.kotest.modules

import mu.KotlinLogging
import net.kotlinx.aws.javaSdkv2.AwsJavaSdkV2Client
import net.kotlinx.aws.javaSdkv2.dynamoLock.DynamoLockManager
import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins.koin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** 해당 패키지의 기본적인 의존성 주입 */
object DynamoLockModule : KoinModule {

    private val log = KotlinLogging.logger {}

    override fun moduleConfig(): Module = module {

        Aws1Module.IAM_PROFILES.profiles.forEach { pair ->
            val profile = pair.first
            single(named(profile)) {
                DynamoLockManager {
                    awsv2 = koin<AwsJavaSdkV2Client>(profile)
                    tableName = "dist_lock-dev"
                    leaseDuration = 2
                    heartbeatPeriod = 1
                    defaultAdditionalTimeout = 4
                }
            }
        }
    }

}