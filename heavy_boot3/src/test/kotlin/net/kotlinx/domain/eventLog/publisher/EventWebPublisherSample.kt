package net.kotlinx.domain.eventLog.publisher

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.kotlinx.aws.AwsInstanceMetadata
import net.kotlinx.domain.eventLog.EventPublishClient
import net.kotlinx.domain.eventLog.EventUtil
import net.kotlinx.domain.menu.Menu
import net.kotlinx.domain.menu.MenuMapping
import net.kotlinx.exception.toSimpleString
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.number.toLocalDateTime
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 이벤트 발생기 - 스프링 시큐리티 필터 버전 샘플
 * */
class EventWebPublisherSample : OncePerRequestFilter() {

    private val client by koinLazy<EventPublishClient>()

    private val instanceMetadata by koinLazy<AwsInstanceMetadata>()

    /**
     * 에러가 아니면서 트랜잭션 내용이 없다면 전송하지 않음
     * */
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        val startTime = System.currentTimeMillis()
        filterChain.doFilter(request, response)

        client.pub { event ->

            val exception = request.getAttribute(EventUtil.ERROR) as Throwable?

            val skipCondition = exception == null && event.datas.isEmpty()
            if (skipCondition) return@pub false  //예외도 아니고, 리드온리 요청이라면 이벤트브릿지 스킵

            event.eventMills = System.currentTimeMillis() - startTime
            event.logLink = instanceMetadata.toLogLinkNotNull(startTime.toLocalDateTime()) //웹 진입 시작시간기준 링크생성

            event.eventStatus = response.status.toString()
            exception?.let { event.errMsg = it.toSimpleString() }

            //            //web 전용
//            val id: Long = Holder.getWebMemberVo().getMember().getId()
//            val loginId: Long = Holder.getWebMemberVo().getLoginMember().getId()
//            event.userId = id.toString() //null일경우 공백으로 입력되게 향후 수정
//            event.userLoginId = loginId.toString() //null일경우 공백으로 입력되게 향후 수정
//            event.eventHash = MemberHashUtil.hashCode(id)
//            val threadInfo: ThreadInfo = Holder.getThreadInfo()
//            val updateIp: String = threadInfo.getUpdateIp()
//            event.clientIp = updateIp

//            if (exception != null) {
//                event.errMsg = ExceptionUtil.toString(exception)
//            }

            val menuMapping = load()
            event.eventDiv = menuMapping?.url ?: request.requestURI


            true
        }


    }

    private fun load(): MenuMapping? = MenuMapping("", "", "", "", "", Menu())


}