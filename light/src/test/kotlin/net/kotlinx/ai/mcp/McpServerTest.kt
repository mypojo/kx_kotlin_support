//package net.kotlinx.ai.mcp
//
//import io.kotest.matchers.shouldBe
//import io.kotest.matchers.shouldNotBe
//import io.kotest.matchers.string.shouldContain
//import io.modelcontextprotocol.kotlin.sdk.shared.CallToolParams
//import io.modelcontextprotocol.kotlin.sdk.shared.CallToolRequest
//import kotlinx.coroutines.test.runTest
//import net.kotlinx.kotest.KotestUtil
//import net.kotlinx.kotest.initTest
//import net.kotlinx.kotest.modules.BeSpecHeavy
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//
//class McpServerTest : BeSpecHeavy() {
//
//    private val mcpDateServer = McpDateServer()
//    private val server = mcpDateServer.getServer()
//
//    init {
//        initTest(KotestUtil.PROJECT)
//
//        Given("MCP Date 서버") {
//
//            When("서버 정보 확인") {
//                Then("올바른 서버 정보를 가져야 함") {
//                    server.serverInfo.name shouldBe "kotlin-date-server"
//                    server.serverInfo.version shouldBe "1.0.0"
//                }
//            }
//
//            When("getCurrentDate 도구 호출 (기본 포맷)") {
//                Then("현재 날짜를 반환해야 함") = runTest {
//                    val request = CallToolRequest(
//                        params = CallToolParams(
//                            name = "getCurrentDate",
//                            arguments = emptyMap()
//                        )
//                    )
//
//                    val result = server.callTool(request)
//
//                    result shouldNotBe null
//                    result.isError shouldBe false
//                    result.content.size shouldBe 1
//
//                    val textContent = result.content[0].text
//                    textContent shouldContain "today"
//                    textContent shouldContain "todayFormatted"
//                    textContent shouldContain "currentDateTime"
//                }
//            }
//
//            When("getCurrentDate 도구 호출 (사용자 지정 포맷)") {
//                Then("지정된 포맷으로 날짜를 반환해야 함") = runTest {
//                    val request = CallToolRequest(
//                        params = CallToolParams(
//                            name = "getCurrentDate",
//                            arguments = mapOf("format" to "yyyy년 MM월 dd일")
//                        )
//                    )
//
//                    val result = server.callTool(request)
//
//                    result shouldNotBe null
//                    result.isError shouldBe false
//                    result.content.size shouldBe 1
//
//                    val textContent = result.content[0].text
//                    textContent shouldContain "년"
//                    textContent shouldContain "월"
//                    textContent shouldContain "일"
//
//                    // 실제 포맷팅 결과도 확인
//                    val today = LocalDate.now()
//                    val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
//                    val expectedFormat = today.format(formatter)
//                    textContent shouldContain expectedFormat
//                }
//            }
//
//            When("잘못된 포맷으로 getCurrentDate 호출") {
//                Then("오류를 반환해야 함") = runTest {
//                    val request = CallToolRequest(
//                        params = CallToolParams(
//                            name = "getCurrentDate",
//                            arguments = mapOf("format" to "잘못된포맷")
//                        )
//                    )
//
//                    val result = server.callTool(request)
//
//                    result shouldNotBe null
//                    result.isError shouldBe true
//                    result.content.size shouldBe 1
//
//                    val textContent = result.content[0].text
//                    textContent shouldContain "날짜 형식 오류"
//                }
//            }
//
//            When("존재하지 않는 도구 호출") {
//                Then("예외가 발생해야 함") = runTest {
//                    val request = CallToolRequest(
//                        params = CallToolParams(
//                            name = "nonExistentTool",
//                            arguments = emptyMap()
//                        )
//                    )
//
//                    try {
//                        server.callTool(request)
//                        throw AssertionError("예외가 발생해야 하는데 발생하지 않았습니다")
//                    } catch (e: Exception) {
//                        e.message shouldContain "Tool not found"
//                    }
//                }
//            }
//
//            When("도구 목록 조회") {
//                Then("getCurrentDate 도구가 포함되어야 함") = runTest {
//                    val tools = server.listTools()
//
//                    tools.tools.size shouldBe 1
//
//                    val tool = tools.tools[0]
//                    tool.name shouldBe "getCurrentDate"
//                    tool.description shouldBe "현재 날짜와 시간을 가져옵니다"
//                    tool.inputSchema shouldNotBe null
//                }
//            }
//        }
//    }
//}