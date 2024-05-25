package net.kotlinx.kotest.modules.ktor.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.li
import kotlinx.html.ul
import net.kotlinx.kotest.modules.ktor.routes.routeDemo

/**
 * http://localhost:8080/
 * http://localhost:8080/static/index.html
 * */
fun Application.configureRouting() {
    install(AutoHeadResponse)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    install(IgnoreTrailingSlash) //후행되는 슬래시(/) 를 무시하게함.  (기본으로는 구분함)
    //routing { 이건 install(Routing){ 하고 동일하다.
    routing {

        //==================================================== 기본설정 ======================================================


        get("/") {
            call.respondRedirect("/index")
        }

        get("/index") {
            call.respondHtml {
                body {
                    h1 { +"메인 데모 화면입니다." }
                    ul {
                        li { +"queryParameters ${call.request.queryParameters.toMap()}" }
                        li { +"headers ${call.request.headers.toMap()}" }
                    }
                }
            }
        }


        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")

        //==================================================== 로직 ======================================================

        routeDemo()

    }
}
