package net.kotlinx.kotest.modules.ktor

import net.kotlinx.core.DataConverter
import net.kotlinx.koin.Koins
import net.kotlinx.ktor.server.KtorJwt

class KtorMemberConverter : DataConverter<KtorMember, String> {

    private val jwt by Koins.koinLazy<KtorJwt>()

    override fun convertTo(data: KtorMember): String = jwt.createToken(
        mapOf(
            KtorMember::name.name to data.name,
            KtorMember::roleGroup.name to data.roleGroup,
        )
    )

    override fun convertFrom(token: String): KtorMember {
        val claimMap = jwt.parseToken(token)
        return KtorMember(
            name = claimMap[KtorMember::name.name]!!.asString(),
            roleGroup = claimMap[KtorMember::roleGroup.name]!!.asString(),
        )
    }


}