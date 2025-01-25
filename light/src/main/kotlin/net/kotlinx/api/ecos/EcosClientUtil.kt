package net.kotlinx.api.ecos

import net.kotlinx.koin.Koins.koin
import net.kotlinx.time.toYmd
import java.math.BigDecimal
import java.time.LocalDate

object EcosClientUtil {

    /**
     * 실시간 달러 원 환율 리턴
     * 원래 API는 기간을 입력해 벌크로 리턴받음.
     *  */
    suspend fun dollarWon(date: String = LocalDate.now().toYmd()): BigDecimal {
        val client = koin<EcosClient>()
        return client.dollarWon()
    }

}
