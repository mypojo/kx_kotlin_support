package net.kotlinx.concurrent

import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class ChannelTest : BeSpecLog() {

    init {
        initTest(KotestUtil.SLOW)
        Given("바닐라 Channel") {

            When("코루틴 취소") {
                Then("코루틴 취소되면 예외 던져짐") {
                    shouldThrow<CancellationException> {
                        coroutineScope {
                            check(isActive)
                            launch {
                                log.debug { "병렬1..." }
                                delay(1000)
                                log.warn { "병렬2...  <-- 이건 호출되지 않음" }
                            }
                            delay(100)
                            cancel()
                            check(!isActive)
                            val channel = Channel<String>(DEFAULT_BUFFER_SIZE)
                            channel.close()
                            check(channel.isClosedForSend)
                            check(channel.isClosedForReceive)
                            log.debug { "여기까지 정상 작동 (suspend를 만나야 취소됨)" }
                            delay(10)
                            log.warn { "여기는 실행 안됨" }
                        }
                        log.info { "예외가 던져짐으로 이건 실행 안됨" }
                    }
                }
            }

            When("배치 처리") {
                Then("요소를 모아서 처리 가능") {
                    val channel = Channel<String>(DEFAULT_BUFFER_SIZE)
                    launch {
                        (0..10).forEach {
                            channel.send("data $it")
                            delay(100)
                        }
                        channel.send("last one")
                        channel.close()
                        log.info("입력 종료")
                        check(channel.isClosedForSend) { "센드는 바로 닫힘" }
                        check(!channel.isClosedForReceive) { "리시브는 데이터가 남아있기때문에 바로 닫히지 않음" }
                    }
                    launch {
                        while (!channel.isClosedForReceive) {
                            val list = channel.tryReceiveAvailable()
                            log.debug { "수신데이터 $list" }
                            delay(250)
                        }
                        log.info("수신 종료")
                    }
                    log.info("별도의 런블로킹 필요없음")
                }

            }
        }
    }


}