package net.kotlinx.kotest

import net.kotlinx.koin.Koins
import net.kotlinx.kotest.modules.Aws1Module
import org.koin.core.module.Module

/**
 * 간단 스펙 재정의
 * koin 기본 오버라이드
 *
 * 이렇게 만든 이유.
 * 1. io.kotest.koin.KoinExtension 사용하지 않음 -> 이경우 Given 마다 koin이 리셋됨
 * 2. 나는 한개의 koin으로 모든 테스트를 수행하고싶음 ex) DB연결 -> 따라서 일반적인 koin으로 사용
 * */
abstract class BeSpecKoin(modules: List<Module>) : BeSpecLog() {

    init {
        beforeSpec {
            Koins.startupOnlyOnce(modules)
        }
    }

    /** sk */
    val findProfile28 by lazy { findProfile("28") }

    /** nd */
    val findProfile46 by lazy { findProfile("46", "80") }

    /** ct */
    val findProfile48 by lazy { findProfile("48") }

    /** ap */
    val findProfile97 by lazy { findProfile("97", "71") }

    /** nb */
    val findProfile99 by lazy { findProfile("99") }

    /** sm */
    val findProfile9780 by lazy { findProfile("97", "80") }


    /**
     * ex)
     * private val profileName by lazy { findProfile99 }
     * private val aws by lazy { koin<AwsClient1>(profileName) }
     * */
    private fun findProfile(id: String, suff: String? = null): String = Aws1Module.IAM_PROFILES.findProfileByAwsId(id, suff)

//    override fun extensions() = listOf(
//        io.kotest.koin.KoinExtension(
//            modules = MyLightKoinStarter.MODULES,
//            mockProvider = null,
//            mode = KoinLifecycleMode.Root,
//        )
//    )

}