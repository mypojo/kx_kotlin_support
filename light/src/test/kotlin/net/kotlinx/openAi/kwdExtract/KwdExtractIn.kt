package net.kotlinx.openAi.kwdExtract

data class KwdExtractIn(val itemId: String, val itemName: String, val theme: String) {
    override fun toString(): String = "$itemId,$itemName,$theme"
}