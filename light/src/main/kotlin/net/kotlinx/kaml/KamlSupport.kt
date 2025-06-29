package net.kotlinx.kaml

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar


/** 이게 기본으로 안됨 */
val YamlNode.content: String
    get() = (this as YamlScalar).content


/** 인라인 도구 */
val YamlNode.toMap: YamlMap
    get() = this as YamlMap