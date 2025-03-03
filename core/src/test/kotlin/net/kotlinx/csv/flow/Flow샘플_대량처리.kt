package net.kotlinx.csv.flow

import kotlinx.coroutines.flow.*
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.concurrent.delay
import net.kotlinx.concurrent.sleep
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.time.measureTimePrint
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class Flow샘플_대량처리 : BeSpecLight() {


    init {
        initTest(KotestUtil.IGNORE)

        val flow = run {
            log.trace { "CSV 파일을 하나로 연결함" }
            val reader1 = (0 until 100).asFlow()
            val reader2 = (100 until 120).asFlow()
            flowOf(reader1, reader2).flattenConcat()
        }

        suspend fun delayRandom() = Random.nextInt(10, 500).milliseconds.delay()
        fun delayRandomForThread() = Random.nextInt(10, 500).milliseconds.sleep()

        val concurrency = 10

        Given("대용량 파일 처리 without spring batch") {

            When("순서 유지하면서 10개씩 병렬 처리 (일반적인 니즈)") {
                //프로세서를 굳이 클래스로 만들어 쓰겠다면  transform 을 alias로 만들것
                val processed = flow.buffer(20).chunked(concurrency).flatMapConcat {
                    flow {
                        log.debug { " -> 데이터 ${it.size}건 입력됨 => ${it} " }
                        it.map { suspend { delayRandom() } }.coroutineExecute() //내부에서 병렬처리
                        //it.map { Callable { delayRandomForThread() } }.parallelExecute() //스래드 처리도 가능
                        emit(it)
                    }
                }
                Then("순서유지는 되지만, 청크내 가장 느린 1개로 병목현상이 발생함") {
                    measureTimePrint {
                        processed.collect {
                            log.info { "최종처리(순서유지) : ${it.size}건 => $it" }
                            log.trace { "결과를 CSV 파일로 분할 & 압축 저장" }
                        }
                    }
                }
            }

            When("순서 무시하면서 10개씩 병렬처리") {
                val processed = flow.buffer(20).flatMapMerge(concurrency) {
                    flow {
                        delayRandom()
                        emit(it)
                    }
                }
                Then("순서 유지는 안되지만 병목 없음 -> 더 빠름") {
                    measureTimePrint {
                        processed.collect {
                            log.info { "최종처리(순서무시) : ${it}건 => $it" }
                        }
                    }
                }
            }


        }


    }

}
