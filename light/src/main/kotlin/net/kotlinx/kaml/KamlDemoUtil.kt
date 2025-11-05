package net.kotlinx.kaml

import com.charleskorn.kaml.*
import mu.KotlinLogging

/**
 * yaml 파일 활용 데모
 * */
object KamlDemoUtil {

    val OPEN_API_METHODS = setOf("get", "post", "put", "delete", "patch", "head", "options", "trace")

    private val log = KotlinLogging.logger {}

    /**
     * 컨트롤러에 1:1로 매핑된 tag를 체크해서, 포함되는것만 남기고 삭제한다
     * 추가로, 남은 경로에서 사용되지 않는 components.schemas 항목을 제거한다.
     * 관리 편의를 위해 처리 중 모든 데이터를 메모리에서 계산한다.
     * */
    fun filterByTag(yamlContent: String, requiredTag: String): String {
        val yamlMap = Yaml.default.parseToYamlNode(yamlContent) as YamlMap

        // 1) paths 를 tag 로 필터링
        val filteredTopEntries = yamlMap.entries.mapNotNull { (scalar1, node1) ->
            when {
                scalar1.content == "paths" && node1 is YamlMap -> {
                    val filteredPaths = node1.entries
                        .filter { it.value is YamlMap }
                        .mapNotNull { (scalar2, node2) ->
                            val byUrl = node2 as YamlMap
                            val filteredByMethod = byUrl.entries.filter { (_, methodValue) ->
                                val tagsNode = (methodValue as? YamlMap)
                                    ?.let { getChild(it, "tags") as? YamlList }
                                val tags = tagsNode?.items?.mapNotNull { (it as? YamlScalar)?.content } ?: emptyList()
                                requiredTag in tags
                            }
                            if (filteredByMethod.isNotEmpty()) scalar2 to YamlMap(filteredByMethod, byUrl.path) else null
                        }
                    if (filteredPaths.isNotEmpty()) scalar1 to YamlMap(filteredPaths.toMap(), node1.path) else null
                }
                else -> scalar1 to node1 // 그 외는 유지 (info, openapi, components 등)
            }
        }

        val filteredRoot = YamlMap(filteredTopEntries.toMap(), yamlMap.path)

        // 2) 남은 paths 로부터 참조된 components.schemas 수집 (초기 집합)
        val pathsNode = getChild(filteredRoot, "paths")
        val initialSchemaRefs = collectSchemaRefs(pathsNode)

        // 3) components.schemas 내에서 참조 전이를 따라가며 필요한 스키마 폐쇄 집합 계산
        val componentsNode = getChild(filteredRoot, "components") as? YamlMap
        val schemasNode = componentsNode?.let { getChild(it, "schemas") as? YamlMap }

        val usedSchemas: Set<String> = if (schemasNode == null) emptySet() else expandSchemaRefs(initialSchemaRefs, schemasNode)

        // 4) components.schemas 정리: 사용되지 않는 항목 제거
        val rebuiltEntries = filteredRoot.entries.mapNotNull { (k, v) ->
            if (k.content != "components") return@mapNotNull k to v

            val compMap = v as? YamlMap ?: return@mapNotNull k to v
            val newSchemasNode: YamlMap? = (getChild(compMap, "schemas") as? YamlMap)
                ?.let { schemas ->
                    if (usedSchemas.isEmpty()) null
                    else {
                        val kept = schemas.entries.filter { it.key.content in usedSchemas }
                        if (kept.isEmpty()) null else YamlMap(kept.toMap(), schemas.path)
                    }
                }

            // components 의 다른 섹션들은 그대로 유지
            val othersPairs = compMap.entries
                .filter { it.key.content != "schemas" }
                .map { it.key to it.value }
            val rebuiltCompEntries: List<Pair<YamlScalar, YamlNode>> = if (newSchemasNode != null) {
                othersPairs + (YamlScalar("schemas", compMap.path) to newSchemasNode)
            } else othersPairs

            if (rebuiltCompEntries.isEmpty()) null else k to YamlMap(rebuiltCompEntries.toMap(), compMap.path)
        }

        val finalRoot = YamlMap(rebuiltEntries.toMap(), filteredRoot.path)

        log.info { "paths 필터링 후 참조된 스키마 수=${initialSchemaRefs.size}, 최종 유지 스키마 수=${usedSchemas.size}" }

        return Yaml.default.encodeToString(YamlMap.serializer(), finalRoot)
    }

    // ===================================== 내부 유틸 =====================================

    /** key 로 자식 노드를 찾는다. */
    private fun getChild(map: YamlMap, key: String): YamlNode? {
        for ((k, v) in map.entries) {
            if (k.content == key) return v
        }
        return null
    }

    /**
     * 임의 노드에서 "$ref: '#/components/schemas/XXX'" 형태를 찾아 스키마 이름 집합으로 변환
     */
    private fun collectSchemaRefs(node: YamlNode?): Set<String> = when (node) {
        null -> emptySet()
        is YamlMap -> node.entries.flatMap { (k, v) ->
            val fromSelf = if (k.content == $$"$ref") {
                val ref = (v as? YamlScalar)?.content
                parseSchemaNameFromRef(ref)?.let { listOf(it) } ?: emptyList()
            } else emptyList()
            val fromChild = collectSchemaRefs(v)
            fromSelf + fromChild
        }.toSet()
        is YamlList -> node.items.flatMap { collectSchemaRefs(it) }.toSet()
        is YamlScalar -> emptySet()
        else -> emptySet()
    }

    /**
     * 스키마 내부도 다른 스키마를 참조하므로, 고정점이 될 때까지 순회하며 폐쇄 집합을 계산한다.
     */
    private fun expandSchemaRefs(initial: Set<String>, schemasNode: YamlMap): Set<String> {
        tailrec fun loop(current: Set<String>): Set<String> {
            val newly = current.flatMap { name ->
                val target = getChild(schemasNode, name)
                collectSchemaRefs(target)
            }.toSet()
            val next = current + newly
            return if (next.size == current.size) current else loop(next)
        }
        return if (initial.isEmpty()) emptySet() else loop(initial)
    }

    /**
     * '#/components/schemas/Name' 형태에서 Name 추출
     */
    private fun parseSchemaNameFromRef(ref: String?): String? = ref
        ?.takeIf { it.startsWith("#/components/schemas/") }
        ?.removePrefix("#/components/schemas/")
}