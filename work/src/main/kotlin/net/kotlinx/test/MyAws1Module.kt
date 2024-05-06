package net.kotlinx.test

import mu.KotlinLogging
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.AwsInfoLoader
import net.kotlinx.aws.athena.AthenaModule
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.core.lib.SystemUtil
import net.kotlinx.koin.KoinModule
import org.koin.core.module.Module
import org.koin.dsl.module

/** 해당 패키지의 기본적인 의존성 주입 */
object MyAws1Module : KoinModule {

    /** 기본 프로파일 */
    var PROFILE_NAME: String? = null

    private val log = KotlinLogging.logger {}

    override fun moduleConfig(): Module = module {
        single {
            val currentProfileName = when {
                PROFILE_NAME == null -> {
                    log.info { "AWS 프로파일 정의가 없습니다" }
                    null
                }

                else -> {
                    val envProfileName = SystemUtil.envValue(PROFILE_NAME!!)
                    if (PROFILE_NAME == envProfileName) {
                        log.info { "프로파일 환경변수 X -> 그대로 사용됨 $PROFILE_NAME" }
                    } else {
                        log.info { "프로파일 환경변수에서 로드됨 $PROFILE_NAME -> $envProfileName" }
                    }
                    envProfileName
                }
            }
            AwsConfig(profileName = currentProfileName)
        }
        single { get<AwsConfig>().toAwsClient1() }
        single { AwsInfoLoader() }
        single { AthenaModule(workGroup = "workgroup-${MyEnv.SUFFIX}", database = MyEnv.SUFFIX.substring(0, 1)) }
    }

}