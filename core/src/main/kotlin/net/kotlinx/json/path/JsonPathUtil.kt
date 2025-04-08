package net.kotlinx.json.path

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.spi.json.GsonJsonProvider
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider
import net.kotlinx.json.gson.GsonSet


object JsonPathUtil {

    val DEFAULT_CONFIGURATION: Configuration by lazy {
        Configuration.builder()
            .jsonProvider(GsonJsonProvider(GsonSet.GSON))
            .mappingProvider(GsonMappingProvider(GsonSet.GSON))
            .options(setOf(Option.SUPPRESS_EXCEPTIONS))
            .build()!!
    }
}