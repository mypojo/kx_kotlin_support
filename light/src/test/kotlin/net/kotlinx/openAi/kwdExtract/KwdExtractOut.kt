package net.kotlinx.openAi.kwdExtract

import mu.KotlinLogging
import net.kotlinx.exception.KnownException
import net.kotlinx.exception.toSimpleString
import net.kotlinx.json.gson.GsonData

data class KwdExtractOut(val itemId: String, val mains: List<String>, val subs: List<String>) {

    companion object {

        private val log = KotlinLogging.logger {}

        fun fromJson(json: GsonData): List<KwdExtractOut> {
            try {
                return json["result"].map { each ->
                    val itemId = each["item_id"].str!!
                    val kwds = each["keywords"]
                    KwdExtractOut(
                        itemId,
                        kwds["main"].map { it.str!!.replace(" ", "") },
                        kwds["sub"].map { it.str!!.replace(" ", "") },
                    )
                }
            } catch (e: Exception) {
                log.warn { "GPT 결과 파싱 실패. ${json.toPreety()}" }
                throw KnownException.ItemRetryException(e.toSimpleString())
            }
        }
    }

}