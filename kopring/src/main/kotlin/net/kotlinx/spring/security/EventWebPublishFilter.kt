package net.kotlinx.spring.security

import net.kotlinx.aws.AwsInfoLoader
import net.kotlinx.core.number.toLocalDateTime
import net.kotlinx.module.eventLog.EventPublisher
import net.kotlinx.module.eventLog.EventUtil
import net.kotlinx.module.eventLog.EventWeb
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 이벤트 발생기 - 스프링 시큐리티 필터 버전 샘플
 * */
class EventWebPublishFilter(
    private val eventPublisher: EventPublisher,
    private val awsInfoLoader: AwsInfoLoader,
) : OncePerRequestFilter() {

    /**
     * 에러가 아니면서 트랜잭션 내용이 없다면 전송하지 않음
     * */
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        val startTime = System.currentTimeMillis()
        filterChain.doFilter(request, response)

        eventPublisher.pub {
            val event = EventUtil.initThreadLocalData(EventWeb())

            val exception = request.getAttribute(EventUtil.ERROR) as Throwable?
            val skipCondition = exception == null && event.datas.isEmpty()
            if (skipCondition) return@pub null

            event.eventMills = System.currentTimeMillis() - startTime
            event.logLink = awsInfoLoader.load().toLogLink(startTime.toLocalDateTime()) //웹 진입 시작시간기준 링크생성

//            //web 전용
//            val id: Long = Holder.getWebMemberVo().getMember().getId()
//            val loginId: Long = Holder.getWebMemberVo().getLoginMember().getId()
//            event.userId = id.toString() //null일경우 공백으로 입력되게 향후 수정
//            event.userLoginId = loginId.toString() //null일경우 공백으로 입력되게 향후 수정
//            event.eventHash = MemberHashUtil.hashCode(id)
//            val threadInfo: ThreadInfo = Holder.getThreadInfo()
//            val updateIp: String = threadInfo.getUpdateIp()
//            event.clientIp = updateIp
//            event.eventStatus = response.status.toString()
//            if (exception != null) {
//                event.errMsg = ExceptionUtil.toString(exception)
//            }
//            val menuMapping: MenuMapping = threadInfo.getMenuMapping()
//            if (menuMapping == null) {
//                val uri = request.requestURI
//                event.eventDiv = uri
//                event.eventName = uri
//                event.author = ""
//            } else {
//                event.eventDiv = menuMapping.getUrl()
//                event.eventName = menuMapping.getDescription()
//                val menu: Menu = menuMapping.getMenu()
//                val developers: List<String> = menu.getDevelopers()
//                event.author = StringUtil.join(developers, ",")
//                if (developers.isEmpty()) {
//                    log.warn("메뉴 등록이 잘못된거 같습니다. 확인 해주세요~  {}", menuMapping.getUrl())
//                }
//            }
            event
        }

    }


}
