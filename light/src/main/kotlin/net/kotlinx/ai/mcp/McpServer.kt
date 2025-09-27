package net.kotlinx.ai.mcp

import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions

class McpDateServer {

    val server = Server(
        serverInfo = Implementation(
            name = "example-server",
            version = "1.0.0"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                resources = ServerCapabilities.Resources(
                    subscribe = true,
                    listChanged = true
                )
            )
        )
    )

    init {
        setupTools()
    }

    private fun setupTools() {

        // Add a resource
        server.addResource(
            uri = "file:///example.txt",
            name = "getCurrentDate",
            description = "현재 날짜와 시간을 가져옵니다",
            mimeType = "text/plain",

            ) { request ->

            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        text = "This is the content of the example resource.",
                        uri = request.uri,
                        mimeType = "text/plain"
                    )
                )
            )
        }
    }

    suspend fun start() {
//        val transport = StdioServerTransport()
//        server.connect(transport)
    }

}
