package net.kotlinx.koin

import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

/**
 * 네이밍 통일용 인터페이스
 * 코인 모듈은 인메모리에서 작동함으로 스래드 안전한 런타임 교체가 불가함 주의!
 *  */
interface KoinModule {
    fun moduleConfig(): Module
}

/** 이미 코인이 실행중인지? */
fun KoinPlatformTools.exist(): Boolean = this.defaultContext().getOrNull() != null

