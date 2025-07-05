package net.kotlinx.core


object ClassLoadSample {

    /** 스프링 MCV 서버 타입 출력 */
    fun detectServerType(): String {
        return if (ClassLoadUtil.exist("org.springframework.web.reactive.function.server.ServerRequest")) {
            "WebFlux"
        } else {
            "MVC"
        }
    }

}