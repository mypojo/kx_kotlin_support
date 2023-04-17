package net.kotlinx.kopring.servlet

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
        val remoteIp: String = this.getHeader("x-forwarded-for")?.let {
            it.split(",").first()
        } ?: this.remoteHost
        check(remoteIp.isNotEmpty()) { "Remote IP is empty" }
        return remoteIp
    }



///**
// * 서블릿 의존 모음
// * @see HttpDomainUtil
// * @see java.net.http.HttpHeaders : 각종 헤더 키값들
// * @see MediaType : 각종 미디어 타입들
// */
//object HttpServletSupport {
//    /** Spring의 메소드를 도용  */
//    fun cacheForSeconds(response: HttpServletResponse, seconds: Int, mustRevalidate: Boolean) {
//        response.setDateHeader(HttpHeaders.EXPIRES, System.currentTimeMillis() + seconds * 1000L)
//        // HTTP 1.1 header
//        var headerValue = "max-age=$seconds"
//        if (mustRevalidate) {
//            headerValue += ", must-revalidate"
//        }
//        response.setHeader(HttpHeaders.CACHE_CONTROL, headerValue)
//    }
//
//    /** 서버로 접속 가능한 URL을 만든다.  */
//    fun rootUrl(request: HttpServletRequest): String {
//        return request.getServerName() + ":" + request.getServerPort()
//    }
//
//    /** 접속 상대경로를 리턴한다.  */
//    fun getUrl(request: HttpServletRequest): String {
//        return request.getRequestURI().substring(request.getContextPath().length)
//    }
//
//    /** 루트의 WEB-INF 경로를 리턴한다.  */
//    fun getRoot(context: ServletContext, path: String?): File {
//        val pathName: String = context.getRealPath(StringUtil.nvl(path, "/"))
//        return File(pathName)
//    }
//
//    /** 웹루트를 리턴한다.  */
//    fun getRoot(req: HttpServletRequest, path: String?): File {
//        return getRoot(req.getSession().getServletContext(), path)
//    }
//
//    /** 루트의 WEB-INF 경로를 리턴한다.  */
//    fun getWebInfoRoot(req: HttpServletRequest?): File {
//        return getRoot(req, "WEB-INF")
//    }
//
//    /** 루트의 WEB-INF 경로를 리턴한다.  */
//    fun getWebInfoRoot(context: ServletContext?): File {
//        return getRoot(context, "WEB-INF")
//    }
//
//    fun getInt(req: HttpServletRequest, key: String?): Int {
//        val value: String = req.getParameter(key)
//        return value.toInt()
//    }
//
//    fun getLong(req: HttpServletRequest, key: String?): Long {
//        val value: String = req.getParameter(key)
//        return value.toLong()
//    }
//
//
//    fun isAjax(req: HttpServletRequest): Boolean {
//        val xRequestWith: String = req.getHeader("x-requested-with") ?: return false
//        return if (xRequestWith == "XMLHttpRequest") true else false
//    }
//
//    /** 간단유틸  */
//    fun getHeaderMap(req: HttpServletRequest): Map<String, String> {
//        val map: MutableMap<String, String> = Maps.newHashMap()
//        val keys: Enumeration<String> = req.getHeaderNames()
//        while (keys.hasMoreElements()) {
//            val key: String = keys.nextElement()
//            map[key] = req.getHeader(key)
//        }
//        return map
//    }
//
//    /** 무조건 String[] 로 들어오는걸 한단계 낮춰준다... 노쓸모  */
//    fun getFlatParameterMap(req: HttpServletRequest): Map<String, String> {
//        val map: MutableMap<String, String> = Maps.newHashMap()
//        for ((key, value) in req.getParameterMap().entries) {
//            if (value.size == 0) map[key] = "" else if (value.size == 1) map[key] = value.get(0) else map[key] = StringUtil.join(value, ",")
//        }
//        return map
//    }
//
//
//    fun getForwardPort(req: HttpServletRequest): Int {
//        val remotePort: Int
//        val remotePortStr: String = req.getHeader("x-forwarded-port")
//        remotePort = if (Strings.isNullOrEmpty(remotePortStr)) req.getRemotePort() else {
//            val proxyIps: List<String> = Lists.newArrayList(SplitUtil.COMMA.split(remotePortStr)) //다운그레이드
//            NumberUtil.parseInt(CollectionUtil.getFirst(proxyIps))
//        }
//        return remotePort
//    }
//
//    fun getForwardProto(req: HttpServletRequest): String? {
//        var remoteProto: String = req.getHeader("x-forwarded-proto")
//        remoteProto = if (Strings.isNullOrEmpty(remoteProto)) req.getScheme() else {
//            val proxyProtos: List<String> = Lists.newArrayList(SplitUtil.COMMA.split(remoteProto)) //다운그레이드
//            CollectionUtil.getFirst(proxyProtos)
//        }
//        return remoteProto
//    }
//
//    fun getUa(req: HttpServletRequest): String {
//        return req.getHeader(HttpHeaders.USER_AGENT)
//    }
//
//    fun getReferer(req: HttpServletRequest): String {
//        return req.getHeader(HttpHeaders.REFERER)
//    }
//
//    /** 간단 호스트 네임 리턴   */
//    fun getHostName(req: HttpServletRequest): String {
//        var scheme: String = req.getHeader("x-forwarded-proto") //스키마가 포워딩 되서 https -> http로 변경될 경우 헤더에 이전 값이 찍힌다. 그냥 getScheme로 호출하면 변경된 http가 찍힘
//        if (scheme == null) scheme = req.getScheme()
//        var host = scheme + "://" + req.getServerName()
//        val port: Int = req.getServerPort()
//        if (port != 80) host += ":$port"
//        return host
//    }
//    /** 세션값 더하기  */
//    /** 세션값 더하기  */
//    @JvmOverloads
//    fun addLong(session: HttpSession, key: String?, value: Long = 1L): Long {
//        val exists: Long = getLong(session, key)
//        val newValue = exists + value
//        session.setAttribute(key, newValue)
//        return newValue
//    }
//
//    /** 세션값 픽스. 필요없는데 깔맞춤 하려고 넣음..  */
//    fun setLong(session: HttpSession, key: String?, value: Long?) {
//        session.setAttribute(key, value)
//    }
//
//    /** 세션값 가져오기. 디폴트는 0  */
//    fun getLong(session: HttpSession, key: String?): Long {
//        return session.getAttribute(key) as Long ?: return 0L
//    }
//
//    /**
//     * 간단 URL 사용
//     * @see javax.ws.rs.core.UriBuilder
//     *
//     */
//    fun getAsString(uri: URI, encode: String?): String {
//        try {
//            uri.toURL().openStream().use { `in` -> return IOUtils.toString(`in`, encode) }
//        } catch (e: IOException) {
//            throw ExceptionUtil.toRuntimeException(e)
//        }
//    }
//
//    /** 강제로 jajx를 resp에 write함  */
//    fun writeJson(resp: HttpServletResponse, json: Any) {
//        resp.setContentType(com.google.common.net.MediaType.JSON_UTF_8.toString()) // .type()은 subtype과 charset을 포함하지 않음
//        HttpServletUtil.cacheForSeconds(resp, -1, true)
//        try {
//            resp.getWriter().write(json.toString())
//        } catch (e: IOException) {
//            throw ExceptionUtil.toRuntimeException(e)
//        }
//    }
//
//    /**
//     * 특정 경로로 스키마를 유지한채 리다이렉트 할때 사용
//     * ALB를 타고 오면 원본 소스가 http 임으로, 서버일경우 무조건 https로 리다이렉트하게 해준다.
//     */
//    fun toHttpsPath(request: HttpServletRequest, redirectPath: String?): String {
//        val serverPort: Int = request.getServerPort()
//        val isValidPort: Boolean = CompareUtil.isEqualsAny(serverPort, 80, 443)
//        val scheme = if (isValidPort) "https" else request.getScheme() //정규 포트이면 https로 무조건 고정
//        val port = if (isValidPort) "" else ":$serverPort"
//        return StringFormatUtil.format("{}://{}{}{}", scheme, request.getServerName(), port, redirectPath)
//    }
//}
