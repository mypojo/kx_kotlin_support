package net.kotlinx.ktor.server.app

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging
import net.kotlinx.system.SystemUtil

fun main() {
    //1. 임베디드 : 설정파일 노필요. 메인함수 노필요. 파라메터에 모듈 전달. 그래들에 main을 열기로 설정해야함
    embeddedServer(
        Netty, port = 8080,
        host = "0.0.0.0",
        module = Application::module,
    )
        .start(wait = true)

}

fun Application.module() {
    configureRouting()

    val log = KotlinLogging.logger {}

    log.info { "서버 시작됨. ${"http://localhost:8080/"}" }
    log.trace {
        SystemUtil.jvmParamPrint()
    }
}
