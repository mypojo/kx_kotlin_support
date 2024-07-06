package net.kotlinx.kotest.modules

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.AwsInstanceMetadataLoader
import net.kotlinx.aws.iam.IamCredential
import net.kotlinx.aws.iam.IamProfiles
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins.koin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** 해당 패키지의 기본적인 의존성 주입 */
object Aws1Module : KoinModule {

    private val log = KotlinLogging.logger {}

    val IAM_PROFILES: IamProfiles by lazy {
        val iamCredential = IamCredential()
        IamProfiles(iamCredential.profileDatas.filter { it.awsId != null }.map { it.profileName to it.awsId!! })
    }

    override fun moduleConfig(): Module = module {


        log.trace { "디폴트 AWS Client 주입" }
        single { AwsConfig() }
        single { koin<AwsConfig>().toAwsClient1() }
        single {
            AwsInstanceMetadataLoader {
                aws = koin<AwsClient1>()
            }.load()
        }

        run {
            log.trace { "프로파일별 AWS Client1 주입" }
            if (log.isTraceEnabled) {
                IAM_PROFILES.printProfiles()
            }

            IAM_PROFILES.profiles.forEach { pair ->
                val profile = pair.first
                single(named(profile)) { AwsConfig(profileName = profile, inputAwsId = pair.second) }
                single(named(profile)) {
                    //thread safe 확인 필요
                    log.debug { "[${profile}] AwsClient1 생성.." }
                    koin<AwsConfig>(profile).toAwsClient1()
                }
                single(named(profile)) {
                    AwsInstanceMetadataLoader {
                        aws = koin<AwsClient1>(profile)
                    }.load()
                }
            }

        }

    }

}