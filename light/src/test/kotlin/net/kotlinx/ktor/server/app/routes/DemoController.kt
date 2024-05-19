package net.kotlinx.ktor.server.app.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.li
import kotlinx.html.ul


fun Routing.routeDemo() {

    route("/demo") {

        get("/user/{userId}") {
            call.response.header("a", "b")
            call.respondHtml {
                body {
                    h1 { +"데모 화면 ${call.request.path()}" }
                    ul {
                        li { +"queryParameters ${call.request.queryParameters.toMap()}" }
                        li { +"pathParameters ${call.parameters.toMap()}" }
                    }
                }
            }
        }

        /** 이렇게 하면 root 는 매칭 안됨 */
        get("/sample01/*") { call.respondText { "sample01" } }

        /** 기본적인 패스매핑. null 지원 */
        get("/sample02/{pathId?}") { call.respondText { "sample02 ${call.parameters["pathId"]}" } }

        /** root 도 매핑해줌 & 다단 패스도 인식해줌 */
        get("/sample03/{paths...}") { call.respondText { "sample02 ${call.parameters.getAll("paths")}" } }
        //정규식은.. 생략

        /** * 뒤에 접미어도 가능  */
        get("""/sample04/*/hello""") { call.respondText { "sample04" } }
    }


}
