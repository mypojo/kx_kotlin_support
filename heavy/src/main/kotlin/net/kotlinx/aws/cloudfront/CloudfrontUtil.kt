package net.kotlinx.aws.cloudfront

import mu.KotlinLogging
import net.kotlinx.file.listAllFiles
import net.kotlinx.okhttp.OkHttpSamples
import java.io.File


object CloudfrontUtil {

    /**
     * 1,000개의 요청 이후에는 1,000개의 URI 당 $0.005의 비용이 부과
     * 간단하게 1개 URI당 1개 파일로 계산
     * @return 리셋 비용(원)
     * */
    fun clearCost(dir: File): Long {
        val dollarWon = OkHttpSamples.dollarWonFetch()
        val fileCnt = dir.listAllFiles().size
        val won = fileCnt / 1000.0 * 0.005 * dollarWon

        val log = KotlinLogging.logger {}
        log.info { "파일수 $fileCnt / 환율 ${dollarWon}원 => 비용 ${won.toLong()}원" }

        return won.toLong()
    }

}

