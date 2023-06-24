package net.kotlinx.spring.servlet

import javax.servlet.http.HttpServletRequest


/**
 * WAS에서 L4나 웹서버의 IP가 찍히는 경우, 포워딩되기전 IP가 필요할때 (HTTP 헤더는 대소문자를 구분하지 않는다)
 * X-Forwarded-For ( XFF )는 HTTP 헤더 필드 ( 영어 ) ??의 하나. HTTP 프록시 서버 또는 부하 분산 장치 (로드 밸런서)를 통해 웹 서버 에 연결하는 클라이언트의 소스 IP 주소 를 특정하기위한 사실상의 표준 이다
 * X-Forwarded-For : client1, proxy1, proxy2  ex) 211.36.151.165, 168.235.200.75
 * 콜론과 공간의 다음에 오는 가장 왼쪽 열에있는 값은 최 하류에 위치하는 클라이언트의 IP 주소, 계속 연결 요청을 전송하는 각각 연속 프록시 연결 요청을 보내는 IP 주소를 추가 간다. 이 예에서는 연결 요청 proxy1, proxy2을 통과하고 마지막에 연결 요청을 원격 주소로 나온다 proxy3에 전해진다.
 * == > 여러 프록시가 존재할 경우 처음것만 리턴한다.
 * 주의!  없으면 getRemoteHost()를 리턴한다. (웹서버를 거치지 않는 경우도 포함)
 */
val HttpServletRequest.forwardedIp: String
    get() {
        val remoteIp: String = this.getHeader("x-forwarded-for")?.split(",")?.first() ?: this.remoteHost
        check(remoteIp.isNotEmpty()) { "Remote IP is empty" }
        return remoteIp
    }


/** 앞에 있는 만료된 쿠기?들은 오버라이드 된다   */
val HttpServletRequest.cookieMap: Map<String, String>
    get() = this.cookies?.let { cookies.associate { it.name to it.value } } ?: emptyMap()

/** HTML 요청인지 여부. ajax 여부를 판단할때 사용된다   */
val HttpServletRequest.isTextHtmlReq: Boolean
    get() = this.getHeader("Accept")?.contains("text/html") ?: false

/**
 * 스키마가 달린 패스를 구한다.
 * ALB를 타고 오면 원본 소스가 http 임으로, 서버일경우 무조건 https를 강제 입력해서 링크 걸어줘야 한다.
 * ex) 특정 경로로 스키마를 유지한채 리다이렉트 할때 사용
 */
fun HttpServletRequest.toPath(redirectPath: String): String {
    val serverPort = this.serverPort
    val validPort: Boolean = serverPort in setOf(80, 443)
    val scheme = if (validPort) "https" else this.scheme //정규 포트이면 https로 강제 변경
    val port = if (validPort) "" else ":$serverPort"
    return "$scheme://${this.serverName}$port$redirectPath"
}