//package net.kotlinx.mcp
//
//import io.modelcontextprotocol.kotlin.sdk.Implementation
//import io.modelcontextprotocol.kotlin.sdk.ReadResourceRequest
//import io.modelcontextprotocol.kotlin.sdk.client.Client
//import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
//import kotlinx.io.asSink
//import kotlinx.io.asSource
//import kotlinx.io.buffered
//import net.kotlinx.kotest.KotestUtil
//import net.kotlinx.kotest.initTest
//import net.kotlinx.kotest.modules.BeSpecHeavy
//
//internal class McpTest : BeSpecHeavy() {
//
//    init {
//        initTest(KotestUtil.PROJECT)
//
//        Given("mcp") {
//
//            // 덕덕고 MCP 서버 프로세스를 직접 실행하거나, 이미 실행 중인 MCP 서버 프로세스에 연결
//            // 예시: ProcessBuilder로 외부 MCP 서버 실행 (경로/옵션은 실제 환경에 맞게 수정)
//            val process = ProcessBuilder("duckduckgo-mcp-server", "--license", "YOUR_LICENSE_KEY")
//                .redirectErrorStream(true)
//                .start()
//
//            // STDIO 전송 방식으로 서버 프로세스와 연결
//            val transport = StdioClientTransport(
//                input = process.inputStream.asSource().buffered(),
//                output = process.outputStream.asSink().buffered(),
//            )
//
//            // MCP 클라이언트 생성
//            val client = Client(
//                clientInfo = Implementation(
//                    name = "duckduckgo-mcp-test-client",
//                    version = "1.0.0"
//                )
//            )
//
//            // 서버 연결
//            client.connect(transport)
//
//            Then("조회 - 1년 전으로부터 최대 100건") {
//                // 리소스 목록 조회
//                val resources = client.listResources()
//                println("Available resources:")
//                resources!!.resources.forEach { println("- ${it.uri} (${it.name})") }
//
//                // 특정 리소스 읽기 (예: "duckduckgo://search?q=Kotlin"과 같은 URI)
//                val resourceUri = "duckduckgo://search?q=Kotlin"
//                val resourceContent = client.readResource(
//                    ReadResourceRequest(uri = resourceUri)
//                )
//                println("Resource content for $resourceUri:")
//                println(resourceContent)
//            }
//
//            client.close()
//        }
//    }
//}