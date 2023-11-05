package net.kotlinx.koin

import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

interface KoinModule : KoinComponent {

    /**
     * 모듈 설정 네이밍 통일용
     * @param option 보통 profile 이름
     * */
    fun moduleConfig(option: String? = null): Module

}

/** 이미 코인이 실행중인지? */
fun KoinPlatformTools.exist(): Boolean = this.defaultContext().getOrNull() != null

