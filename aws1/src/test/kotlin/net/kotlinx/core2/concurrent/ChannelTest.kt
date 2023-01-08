package net.kotlinx.core2.concurrent

import ch.qos.logback.classic.Level
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import net.kotlinx.core1.CoreUtil
import net.kotlinx.core2.logback.LogBackUtil

internal class ChannelTest : DescribeSpec({

    val log = KotlinLogging.logger {}
    LogBackUtil.logLevelTo(CoreUtil.packageName, Level.DEBUG)

    describe("바닐라 Channel") {

        context("코루틴 취소") {
            it("코루틴 취소되면 예외 던져짐") {
                shouldThrow<CancellationException> {
                    coroutineScope {
                        assert(isActive)
                        launch {
                            log.debug { "병렬1..." }
                            delay(1000)
                            log.warn { "병렬2...  <-- 이건 호출되지 않음" }
                        }
                        delay(100)
                        cancel()
                        assert(!isActive)
                        val channel = Channel<String>(DEFAULT_BUFFER_SIZE)
                        channel.close()
                        assert(channel.isClosedForSend)
                        assert(channel.isClosedForReceive)
                        log.debug { "여기까지 정상 작동 (suspend를 만나야 취소됨)" }
                        delay(10)
                        log.warn { "여기는 실행 안됨" }
                    }
                    log.info { "예외가 던져짐으로 이건 실행 안됨" }
                }
            }
        }

        context("배치 처리") {
            it("요소를 모아서 처리 가능") {
                val channel = Channel<String>(DEFAULT_BUFFER_SIZE)
                launch {
                    (0..10).forEach {
                        channel.send("data $it")
                        delay(1000)
                    }
                    channel.send("last one")
                    channel.close()
                    log.info("입력 종료")
                    assert(channel.isClosedForSend) { "센드는 바로 닫힘" }
                    assert(!channel.isClosedForReceive) { "리시브는 데이터가 남아있기때문에 바로 닫히지 않음" }
                }
                launch {
                    while (!channel.isClosedForReceive) {
                        val list = channel.tryReceiveAvailable()
                        log.debug { "수신데이터 $list" }
                        delay(2500)
                    }
                    log.info("수신 종료")
                }
                log.info("별도의 런블로킹 필요없음")
            }

        }
    }

})