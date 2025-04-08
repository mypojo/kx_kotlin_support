package net.kotlinx.json.path

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.TypeRef


/** json 파싱 */
fun Configuration.parse(json: Any): DocumentContext = JsonPath.using(this).parse(json.toString())

/** path 파싱 */
fun <T> DocumentContext.pathTo(path: String): T? = this.read(path, object : TypeRef<T>() {})

/**
 * path 파싱
 * 디폴트로 객체 리턴시 JsonObject (gson)
 *  */
fun DocumentContext.path(path: String): Any? = this.read(path)

