package net.kotlinx.test

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.AwsInfoLoader
import net.kotlinx.aws.athena.AthenaModule
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.koin.KoinModule
import org.koin.core.module.Module
import org.koin.dsl.module

/** 해당 패키지의 기본적인 의존성 주입 */
object MyAws1Module : KoinModule {

    override fun moduleConfig(profileName: String?): Module = module {

        val awsConfig = AwsConfig(profileName = profileName)
        single { awsConfig.toAwsClient1() }
        single { AwsInfoLoader() }
        single { AthenaModule(workGroup = "workgroup-${MyEnv.SUFFIX}", database = MyEnv.SUFFIX.substring(0, 1)) }
    }

}