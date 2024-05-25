package net.kotlinx.aws.lambdaUrl

import aws.smithy.kotlin.runtime.http.HttpStatusCode
import com.amazonaws.services.lambda.runtime.Context
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import mu.KotlinLogging
import net.kotlinx.aws.lambda.LambdaMapResult
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.exception.toSimpleString
import net.kotlinx.html.setDefault
import net.kotlinx.json.gson.GsonData
import net.kotlinx.string.ResultText


/**
 * 간단 라우터 등록기
 * http 요청에 응답한다. ex) API Gateway or 람다 URL
 * 해당 람다에는 이거 하나만 보통 등록된다.
 *
 * 주의!
 *
 *  */
@Deprecated("xx")
class FunctionRouter(block: FunctionRouter.() -> Unit = {}) : LambdaLogicHandler {

    private val inputRoutes = mutableListOf<FunctionRouteInfo>()

    /** 라우팅 등록 */
    fun route(block: FunctionRouteInfo.() -> Unit = {}) {
        inputRoutes += FunctionRouteInfo().apply(block)
    }

    /** IP 체크 등의 벨리데이션 체크 */
    var authCheck: (LambdaUrlInput) -> ResultText = { ResultText(true, "-") }

    //==================================================== 예외 처리기 ======================================================

    /** 기본 인증실패 처리기 */
    var authFailHandler: (LambdaUrlInput, String) -> LambdaMapResult = { _, msg ->
        val html = createHTML().html {
            setDefault("잘못된 요청입니다")
            body {
                h1 { +msg }
            }
        }
        LambdaUrlOutput(html, HttpStatusCode.Forbidden.value) //403
    }

    /**
     * 기본 낫파운드 처리기
     * 기본 처리기로 동작한다. 이거 하나만 있을때도 있음
     *  */
    var notFoundHandler: (LambdaUrlInput) -> LambdaMapResult = {
        val code = HttpStatusCode.NotFound
        val html = createHTML().html {
            setDefault("${code.value} [${code.description}] 잘못된 요청입니다")
            body {
                h1 { +"path 매칭 실패 ${it.path}" }
            }
        }
        LambdaUrlOutput(html, code.value)
    }

    /** 기본 500 예외 처리기 */
    var errorHandler: (LambdaUrlInput, Throwable) -> LambdaMapResult = { i, e ->
        e.printStackTrace()
        val html = createHTML().html {
            setDefault("오류가 발생했습니다.")
            body {
                h1 { +"오류가 발생했습니다. ${i.path} -> ${e.toSimpleString()}" }
            }
        }
        LambdaUrlOutput(html, HttpStatusCode.InternalServerError.value)
    }

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
            return authFailHandler(data, authCheckResult.result)
        }

        val path = data.path
        val route = run {
            if (path.isEmpty() || path == "/") return@run root
            routes.firstOrNull { path.startsWith(it.pathPrefix) }
        } ?: return notFoundHandler(data)

        return try {
            route.process(data)
        } catch (e: Throwable) {
            errorHandler.invoke(data, e)
        }

    }


}
