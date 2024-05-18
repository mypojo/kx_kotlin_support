package net.kotlinx.aws.sts

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koinLazy


object StsUtil {

    private val log = KotlinLogging.logger {}

    /**
     * AWS의 계정 고유 ID
     * 주요 리소스의 경로 path에 사용된다
     * */
    val ACCOUNT_ID: String by lazy {
        val aws by koinLazy<AwsClient1>()
        runBlocking {
            val identity = aws.sts.getCallerIdentity()
            log.debug { "AWS ID 로드 : ${identity.account}" }
            identity.account!!
        }
    }

}