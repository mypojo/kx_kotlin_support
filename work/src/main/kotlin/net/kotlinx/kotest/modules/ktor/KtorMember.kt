package net.kotlinx.kotest.modules.ktor

import io.ktor.server.auth.*

data class KtorMember(val name: String, val roleGroup: String) : Principal