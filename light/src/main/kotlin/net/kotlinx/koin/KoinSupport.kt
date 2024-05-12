package net.kotlinx.koin

import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

/** 네이밍 통일용 인터페이스 */
interface KoinModule {
    fun moduleConfig(): Module
}

/** 이미 코인이 실행중인지? */
fun KoinPlatformTools.exist(): Boolean = this.defaultContext().getOrNull() != null

