package net.kotlinx.kotest.modules

import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins.koin
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

interface KoinData : KoinComponent

class KoinData1(val name: String) : KoinData
class KoinData2(val name: String) : KoinData
class KoinData3(val name: String) : KoinData
class KoinData4(val name: String) : KoinData

class KoinData5<T>(val name: String)

class PooService {
    val data2: KoinData2 = koin()
    val ex01: KoinData1 = koin("ex01")
}

/** 해당 패키지의 기본적인 의존성 주입 */
object KoinTestModule : KoinModule {

    override fun moduleConfig(): Module = module {

        //single { KoinData1("KoinData4") as KoinData } // as 표현식 가능 -> KoinData4 가 아닌 KoinData 로 들어감 but IDE에서 필요없다고 체크하기때문에 이렇게 금지

        single { KoinData1("테스트1") } bind KoinData::class
        single { KoinData1("테스트1-오버라이드") } bind KoinData::class
        single(named("ex01")) { KoinData1("테스트1-ex01") } bind KoinData::class //루트는 생성되지 않음

        single { KoinData2("테스트2") } bind KoinData::class
        single { KoinData2("테스트2-ex02") } withOptions {  //withOptions 은 루트도 입력함
            this.named("ex02") //이름 같은게 여러개 있으니 주의!
            createdAtStart() //시작할때 미리 생성됨
        }

        single { (name: String) -> KoinData3(name) } //파라메터 커스텀 가능

        single(named("ex04")) { KoinData4("테스트1-ex04") } bind KoinData::class //루트는 생성되지 않음

        single { PooService() }

        single(named("ex01")) { DeveloperData(id = "kim") }

        single { KoinData5<KoinData1>("data01") }
        single { KoinData5<KoinData2>("data02") }

    }

}