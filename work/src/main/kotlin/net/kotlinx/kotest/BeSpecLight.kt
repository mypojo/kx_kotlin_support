package net.kotlinx.kotest

import io.kotest.koin.KoinLifecycleMode
import net.kotlinx.test.MyLightKoinStarter
import org.koin.core.component.KoinComponent

/**
 * 간단 스펙 재정의
 * koin 기본 오버라이드
 * */
abstract class BeSpecLight : BeSpecLog(), KoinComponent {

    override fun extensions() = listOf(
        io.kotest.koin.KoinExtension(
            modules = MyLightKoinStarter.MODULES,
            mockProvider = null,
            mode = KoinLifecycleMode.Root,
        )
    )

}