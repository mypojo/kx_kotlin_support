package net.kotlinx.kotest.modules

import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins.koin
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

interface KoinData : KoinComponent

class KoinData1(val name: String) : KoinData
class KoinData2(val name: String) : KoinData
class KoinData3(val name: String) : KoinData
class PooService {
    val data2: KoinData2 = koin()
    val ex01: KoinData1 = koin("ex01")
}

/** 해당 패키지의 기본적인 의존성 주입 */
object MyKoinTestModule : KoinModule {

    override fun moduleConfig(): Module = module {
        single { KoinData1("테스트1") } bind KoinData::class
        single { KoinData2("테스트2") } bind KoinData::class
        single { KoinData1("테스트1-1") } bind KoinData::class
        single(named("ex01")) { KoinData1("특수관리") }
        single(named("ex03")) { KoinData3("특수관리") }
        single { PooService() }
    }

}