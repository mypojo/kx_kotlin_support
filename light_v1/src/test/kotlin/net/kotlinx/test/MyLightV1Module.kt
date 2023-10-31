package net.kotlinx.test

import net.kotlinx.koin.KoinModule
import org.koin.core.module.Module
import org.koin.dsl.module

/** 해당 패키지의 기본적인 의존성 주입 */
object MyLightV1Module : KoinModule {

    override fun moduleConfig(profileName: String?): Module = module {

        //==================================================== 기본 ======================================================


    }

}