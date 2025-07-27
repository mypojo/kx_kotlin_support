package net.kotlinx.kaml

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap

/**
 * yaml 파일 활용 데모
 * */
object KamlDemoUtil {

    val OPEN_API_METHODS = setOf("get", "post", "put", "delete", "patch", "head", "options", "trace")

    /**
     * 컨트롤러에 1:1로 매핑된 tag를 체크해서, 포함되는것만 남기고 삭제한다
     * */
    fun filterByTag(yamlContent: String, requiredTag: String): String {
        val yamlMap = Yaml.default.parseToYamlNode(yamlContent) as YamlMap

        val filteredEntries = yamlMap.entries.mapNotNull { (scalar1, node1) ->
            when {
                scalar1.content == "paths" && node1 is YamlMap -> {
                    //메소드로 간주함
                    val filteredPaths = node1.entries.filter { it.value is YamlMap }.mapNotNull { (scalar2, node2) ->
                        val byUrl = node2 as YamlMap  //URL 단위로 분리되어있음
                        val filterdByMethod = byUrl.entries.filter { (methodKey, methodValue) ->
                            val tags = methodValue.toMap.entries.entries.firstOrNull { it.key.content == "tags" }
                                ?.value.let { it as YamlList }.items.map { it.content } ?: emptyList()
                            requiredTag in tags
                        }

                        // 필터링된 메소드가 있는 경우만 경로 유지
                        if (filterdByMethod.isNotEmpty()) {
                            scalar2 to YamlMap(filterdByMethod, byUrl.path)
                        } else {
                            null
                        }
                    }

                    // 필터링된 경로가 있는 경우만 paths 섹션 유지
                    if (filteredPaths.isNotEmpty()) {
                        scalar1 to YamlMap(filteredPaths.toMap(), node1.path)
                    } else {
                        null
                    }
                }

                else -> {
                    scalar1 to node1 // paths가 아닌 다른 섹션들은 그대로 유지 (info, openapi, components 등)
                }
            }
        }

        return Yaml.default.encodeToString(YamlMap.serializer(), YamlMap(filteredEntries.toMap(), yamlMap.path))
    }
}