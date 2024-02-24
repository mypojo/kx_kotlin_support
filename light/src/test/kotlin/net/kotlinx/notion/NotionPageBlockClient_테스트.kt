package net.kotlinx.notion

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.ssm.find
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class NotionPageBlockClient_테스트 : TestRoot() {

    val aws = AwsConfig().toAwsClient1()
    val secretValue by lazy {
        runBlocking {
            aws.ssm.find("/notion/key")!!
        }
    }

    @Test
    fun 블록조회() {
        val page = NotionPageBlockClient(secretValue)

        runBlocking {
            val blocks = page.blocks("4b18e3f52ce84487b64acab8ab2b5837", 1)
            blocks.forEach {
                log.info { "value = ${it.body}" }
            }
        }

        //https://www.notion.so/mypojo/4b18e3f52ce84487b64acab8ab2b5837?pvs=4#f9405aee1811461884660aa6832e2cf0
        //https://www.notion.so/mypojo/4b18e3f52ce84487b64acab8ab2b5837?pvs=4#f9405aee1811461884660aa6832e2cf0


    }

    @Test
    fun 블록수정() {
        runBlocking {
//            val page = NotionPageBlockClient(secretValue)
//            page.update(
//                NotionCell2(
//                    "f9405aee-1811-4618-8466-0aa6832e2cf0",
//                    NotionCellType.rich_text,
//                    "최근동기화시간 = ${LocalDateTime.now().toF01()}"
//                )
//            )
        }
    }

}