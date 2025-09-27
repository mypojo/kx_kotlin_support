package net.kotlinx.ai.mcp

import kotlinx.coroutines.runBlocking

suspend fun main() {
    System.err.println("Kotlin MCP Date Server started...")

    try {
        val mcpServer = McpDateServer()
        mcpServer.start()
    } catch (e: Exception) {
        System.err.println("Error starting MCP server: ${e.message}")
        e.printStackTrace()
    }

    System.err.println("Kotlin MCP Date Server stopped.")
}

fun main(args: Array<String>) = runBlocking {
    main()
}