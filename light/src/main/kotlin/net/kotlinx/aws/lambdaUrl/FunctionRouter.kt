package net.kotlinx.aws.lambdaUrl

import com.amazonaws.services.lambda.runtime.Context
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import mu.KotlinLogging
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler

import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.html.setDefault
import net.kotlinx.core.string.ResultText


/**
 * 간단 라우터 등록기
 *  */
class FunctionRouter(block: FunctionRouter.() -> Unit = {}) : LambdaLogicHandler {

    private val inputRoutes = mutableListOf<FunctionRouteInfo>()

    /** 라우팅 등록 */
    fun route(block: FunctionRouteInfo.() -> Unit = {}) {
        inputRoutes += FunctionRouteInfo().apply(block)
    }

    /** IP 체크 등의 벨리데이션 체크 */
    var authCheck: (LambdaUrlInput) -> ResultText = { ResultText(true, "-") }

    init {
        block(this)
    }

    private val log = KotlinLogging.logger {}

    /** 프리픽스 체크라서, 루트는 별도 관리 */
    private val root: FunctionRouteInfo = inputRoutes.firstOrNull { it.pathPrefix == "/" } ?: throw IllegalStateException("Root 라우팅을 입력해주세요")

    /** 매칭할 전체 루트 */
    private val routes: List<FunctionRouteInfo> = inputRoutes.filter { it.pathPrefix != "/" }

    /** URL 입력이면 라우팅 해줌 */
    override suspend fun invoke(input: GsonData, context: Context?): Any? {

        LambdaUrlInput.extractPath(input) ?: return null

        val data = try {
            LambdaUrlInput(input)
        } catch (e: Exception) {
            log.warn { "LambdaUrlInput 파싱 실패!  입력값 $input" }
            throw e
        }
        log.debug { "입력 [${data.path}] ${data.ip} : ${data.query}" }

        val authCheckResult = authCheck(data)
        if (!authCheckResult.ok) {
            val html = createHTML().html {
                setDefault("잘못된 요청입니다")
                body {
                    h1 { +authCheckResult.result }
                }
            }
            return LambdaUrlOutput(html, 403)
        }

        val path = data.path
        val route = run {
            if (path.isEmpty() || path == "/") return@run root
            routes.firstOrNull { path.startsWith(it.pathPrefix) }
        }
        if (route == null) {
            val html = createHTML().html {
                setDefault("잘못된 요청입니다")
                body {
                    h1 { +"path 매칭 실패 ${data.path}" }
                }
            }
            return LambdaUrlOutput(html, 400)
        }
        return route.process(data)

    }


}
