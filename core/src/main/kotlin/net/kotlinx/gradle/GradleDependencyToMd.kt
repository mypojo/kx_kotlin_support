package net.kotlinx.gradle

import mu.KotlinLogging
import net.kotlinx.core.VibeCoding
import java.io.File

/**
 * Gradle dependencies 출력 결과를 파싱하여 MD 파일로 변환하는 클래스
 */
@VibeCoding
class GradleDependencyToMd {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    /**
     * 의존성 섹션 정의
     * @param title 섹션 제목
     * @param fileName 저장할 파일명 (예: "aws.md")
     * @param filter 필터 함수 (null이면 다른 필터에서 선택받지 못한 모든 데이터 포함)
     */
    data class DependencySection(
        val title: String,
        val fileName: String,
        val filter: ((DependencyInfo) -> Boolean)? = null,
    )

    /**
     * 의존성 정보를 담는 데이터 클래스
     */
    data class DependencyInfo(
        val groupId: String,
        val artifactId: String,
        /** 선택 정보 포함 */
        val version: String,
        val scope: String,
        /** root인경우 null */
        val parent: DependencyInfo? = null,
    ) {
        /**
         * 출처 경로를 생성 (부모 체인을 역순으로)
         * 2단계까지만 표시하고 더 길면 "-> ..." 추가
         */
        fun getSourcePath(): String {
            // 부모가 없으면 빈 문자열 (root 의존성)
            if (parent == null) return ""

            // 부모 체인을 역순으로 생성 (자신의 부모부터 root까지)
            val parentChain = generateSequence(parent) { it.parent }
                .toList()
                .reversed()

            // 2단계까지만 표시, 더 길면 "..." 표시
            return when {
                parentChain.size <= 2 -> {
                    parentChain.joinToString(" -> ") { "${it.groupId}:${it.artifactId}" }
                }

                else -> {
                    val firstTwo = parentChain.take(2)
                    firstTwo.joinToString(" -> ") { "${it.groupId}:${it.artifactId}" } + " -> ..."
                }
            }
        }
    }

    /**
     * compileClasspath 섹션을 콘솔 로그에서 추출
     */
    fun extractCompileClasspathLines(consoleLog: List<String>): List<String> {
        return consoleLog
            .dropWhile { !it.startsWith("compileClasspath") }  // compileClasspath 줄까지 건너뛰기
            .drop(1)  // compileClasspath 줄 자체는 제외
            .takeWhile { it.isNotBlank() }  // 빈 줄이 나올 때까지 가져오기
    }

    /**
     * runtimeClasspath 섹션을 콘솔 로그에서 추출
     */
    fun extractRuntimeClasspathLines(consoleLog: List<String>): List<String> {
        return consoleLog
            .dropWhile { !it.startsWith("runtimeClasspath") }  // runtimeClasspath 줄까지 건너뛰기
            .drop(1)  // runtimeClasspath 줄 자체는 제외
            .takeWhile { it.isNotBlank() }  // 빈 줄이 나올 때까지 가져오기
    }

    /**
     * 트리 depth 계산
     */
    private fun calculateDepth(line: String): Int {
        val treeChars = line.takeWhile { it == '|' || it == '+' || it == '\\' || it == '-' || it == ' ' }
        // 5자리 단위로 depth 계산 (예: "|    " = 5자리 = 1 depth)
        return treeChars.length / 5
    }

    /**
     * 의존성 문자열 파싱
     */
    private fun parseDependencyLine(line: String): Triple<String, String, String>? {
        // 트리 구조 문자 제거
        val content = line.replace(Regex("^[|+\\\\\\- ]+"), "").trim()
        if (content.isEmpty()) return null

        // 괄호나 추가 정보 제거 전 원본 저장
        val mainPart = content.split(" (")[0].trim()

        // project :core 같은 경우 처리
        if (mainPart.startsWith("project ")) {
            val projectName = mainPart.removePrefix("project ")
            return Triple("project", projectName, "")
        }

        // groupId:artifactId:version 형식 파싱
        val parts = mainPart.split(":")
        return when {
            parts.size >= 3 -> {
                val groupId = parts[0]
                val artifactId = parts[1]
                val version = parts.drop(2).joinToString(":")
                Triple(groupId, artifactId, version)
            }

            parts.size == 2 -> {
                // groupId:artifactId만 있는 경우
                Triple(parts[0], parts[1], "")
            }

            else -> null
        }
    }

    /**
     * 의존성 라인들을 파싱하여 DependencyInfo 리스트 생성
     */
    fun parseDependencies(lines: List<String>, scope: String = "컴파일"): List<DependencyInfo> {
        return buildList {
            val parentStack = mutableListOf<Pair<Int, DependencyInfo>>() // (depth, info)

            lines.forEach { line ->
                val parsed = parseDependencyLine(line) ?: return@forEach
                val depth = calculateDepth(line)
                val (groupId, artifactId, version) = parsed

                // 현재 depth보다 깊은 항목들은 스택에서 제거
                while (parentStack.isNotEmpty() && parentStack.last().first >= depth) {
                    parentStack.removeLast()
                }

                // 부모 찾기 (스택의 마지막 항목)
                val parent = parentStack.lastOrNull()?.second

                val info = DependencyInfo(
                    groupId = groupId,
                    artifactId = artifactId,
                    version = version,
                    scope = scope,
                    parent = parent
                )

                add(info)
                parentStack.add(depth to info)
            }
        }
    }

    /**
     * 의존성의 전체 경로를 생성 (root부터 현재까지)
     * parent 관계를 고려한 고유 식별자로 사용
     */
    private fun DependencyInfo.getFullPath(): String {
        val parentPath = parent?.getFullPath() ?: ""
        val currentNode = "$groupId:$artifactId:$version"
        return if (parentPath.isEmpty()) currentNode else "$parentPath -> $currentNode"
    }

    /**
     * 두 의존성 리스트를 병합하고 scope을 통합
     * - 둘 다 있으면: "컴파일/런타임"
     * - 하나만 있으면: "컴파일" 또는 "런타임"
     */
    fun mergeDependencies(
        compileDependencies: List<DependencyInfo>,
        runtimeDependencies: List<DependencyInfo>
    ): List<DependencyInfo> {
        // 모든 의존성을 합친 후 전체 경로로 그룹핑
        val allDependencies = compileDependencies + runtimeDependencies

        return allDependencies
            .groupBy { it.getFullPath() }
            .map { (_, deps) ->
                // 같은 경로의 의존성들에서 scope 결정
                val scopes = deps.map { it.scope }.toSet()
                val mergedScope = when {
                    scopes.contains("컴파일") && scopes.contains("런타임") -> "컴파일/런타임"
                    scopes.contains("컴파일") -> "컴파일"
                    else -> "런타임"
                }

                // 첫 번째 항목을 기준으로 새 DependencyInfo 생성 (scope만 변경)
                deps.first().copy(scope = mergedScope)
            }
    }

    /**
     * 의존성 리스트를 마크다운 테이블 형태로 변환 (단일 섹션)
     * @param dependencies 의존성 리스트
     * @param filter 필터 함수 (null이면 모두 포함)
     */
    fun generateMarkdownTable(
        dependencies: List<DependencyInfo>,
        filter: ((DependencyInfo) -> Boolean)? = null,
    ): String {
        return buildString {
            // 헤더
            appendLine("| scope | 그룹 | 이름 | 버전 | 출처 |")
            appendLine("|-------|------|------|------|------|")

            // 필터링된 의존성 추출
            val filteredDeps = dependencies.filter { dep ->
                filter?.invoke(dep) ?: true
            }

            // groupId, artifactId, version으로 그룹핑 후 정렬
            val groupedDeps = filteredDeps
                .groupBy { dep -> Triple(dep.groupId, dep.artifactId, dep.version) }
                .toList()
                .sortedWith(compareBy({ it.first.first }, { it.first.second }))  // groupId -> artifactId 순 정렬

            // 데이터 행 생성
            groupedDeps.forEach { (key, deps) ->
                val (groupId, artifactId, version) = key
                val scope = deps.first().scope

                // 출처 추출 (빈 문자열 제외)
                val sourcePaths = deps
                    .map { it.getSourcePath() }
                    .filter { it.isNotEmpty() }
                    .distinct()

                // project로 시작하지 않는 출처만 필터링
                val nonProjectSources = sourcePaths.filter { !it.startsWith("project:") }

                // 원래 출처가 없거나(root 의존성), 필터링 후에도 남은 출처가 있으면 표시
                if (sourcePaths.isEmpty() || nonProjectSources.isNotEmpty()) {
                    val sourcePathsStr = nonProjectSources.joinToString("<br>")
                    appendLine("| $scope | $groupId | $artifactId | $version | $sourcePathsStr |")
                }
            }
        }
    }

    /**
     * 의존성 리스트를 섹션별로 나누어 마크다운으로 변환
     * @param dependencies 의존성 리스트
     * @param sections 섹션 정의 리스트
     * @param globalFilter 전역 필터 (모든 섹션에 공통 적용)
     */
    fun generateMarkdownWithSections(
        dependencies: List<DependencyInfo>,
        sections: List<DependencySection>,
        globalFilter: ((DependencyInfo) -> Boolean)? = null,
    ): String {
        val sectionMarkdowns = sections.mapNotNull { section ->
            // 필터링된 의존성 추출
            val filteredDeps = dependencies.filter { dep ->
                val passGlobal = globalFilter?.invoke(dep) ?: true
                val passSection = section.filter?.invoke(dep) ?: true
                passGlobal && passSection
            }

            // groupId, artifactId, version으로 그룹핑 후 정렬
            val groupedDeps = filteredDeps
                .groupBy { dep -> Triple(dep.groupId, dep.artifactId, dep.version) }
                .toList()
                .sortedWith(compareBy({ it.first.first }, { it.first.second }))  // groupId -> artifactId 순 정렬

            // 필터링된 데이터 행 생성
            val dataRows = buildList {
                groupedDeps.forEach { (key, deps) ->
                    val (groupId, artifactId, version) = key
                    val scope = deps.first().scope

                    // 출처 추출 (빈 문자열 제외)
                    val sourcePaths = deps
                        .map { it.getSourcePath() }
                        .filter { it.isNotEmpty() }
                        .distinct()

                    // project로 시작하지 않는 출처만 필터링
                    val nonProjectSources = sourcePaths.filter { !it.startsWith("project:") }

                    // 원래 출처가 없거나(root 의존성), 필터링 후에도 남은 출처가 있으면 표시
                    if (sourcePaths.isEmpty() || nonProjectSources.isNotEmpty()) {
                        val sourcePathsStr = nonProjectSources.joinToString("<br>")
                        add("| $scope | $groupId | $artifactId | $version | $sourcePathsStr |")
                    }
                }
            }

            // 데이터가 없으면 null 반환
            if (dataRows.isEmpty()) {
                null
            } else {
                buildString {
                    // 섹션 제목
                    appendLine("## ${section.title}")
                    appendLine()

                    // 헤더
                    appendLine("| scope | 그룹 | 이름 | 버전 | 출처 |")
                    appendLine("|-------|------|------|------|------|")

                    // 데이터 행 추가
                    dataRows.forEach { appendLine(it) }
                }
            }
        }

        return sectionMarkdowns.joinToString("\n")
    }

    /**
     * 콘솔 로그 파일을 읽어 MD 파일로 변환 (단일 테이블)
     * @param consoleLogFile gradle dependencies 콘솔 로그 파일
     * @param outputFile 출력할 MD 파일
     * @param filter 필터 함수 (null이면 모두 포함)
     */
    fun convertToMarkdown(
        consoleLogFile: File,
        outputFile: File,
        filter: ((DependencyInfo) -> Boolean)? = null,
    ) {
        log.info { "의존성 분석 시작: ${consoleLogFile.absolutePath}" }

        // 콘솔 로그 읽기
        val consoleLog = consoleLogFile.readLines()

        // compileClasspath 섹션 추출
        val compileClasspathLines = extractCompileClasspathLines(consoleLog)
        log.info { "compileClasspath 의존성 라인 수: ${compileClasspathLines.size}" }

        // 의존성 파싱
        val dependencies = parseDependencies(compileClasspathLines)
        log.info { "파싱된 의존성 개수: ${dependencies.size}" }

        // MD 테이블 생성
        val mdContent = generateMarkdownTable(dependencies, filter)

        // 파일 저장
        outputFile.writeText(mdContent, Charsets.UTF_8)
        log.info { "MD 파일 생성 완료: ${outputFile.absolutePath}" }

        // 필터링 후 개수
        val filteredCount = dependencies.count { filter?.invoke(it) ?: true }
        log.info { "총 ${filteredCount}개의 의존성이 기록되었습니다." }
    }

    /**
     * 콘솔 로그 파일을 읽어 섹션별로 MD 파일로 변환 (섹션별 파일 분리)
     * @param consoleLogFile gradle dependencies 콘솔 로그 파일
     * @param rootDir 출력할 디렉토리 (섹션별 파일이 여기에 저장됨)
     * @param sections 섹션 정의 리스트 (필터가 null인 섹션은 최대 1개, 마지막에 처리)
     * @param globalFilter 전역 필터 (모든 섹션에 공통 적용)
     */
    fun convertToMd(
        consoleLogFile: File,
        rootDir: File,
        sections: List<DependencySection>,
        globalFilter: ((DependencyInfo) -> Boolean)? = null,
    ) {
        log.info { "의존성 분석 시작: ${consoleLogFile.absolutePath}" }

        // 콘솔 로그 읽기
        val consoleLog = consoleLogFile.readLines()

        // compileClasspath 섹션 추출 및 파싱
        val compileClasspathLines = extractCompileClasspathLines(consoleLog)
        log.info { "compileClasspath 의존성 라인 수: ${compileClasspathLines.size}" }
        val compileDependencies = parseDependencies(compileClasspathLines, scope = "컴파일")

        // runtimeClasspath 섹션 추출 및 파싱
        val runtimeClasspathLines = extractRuntimeClasspathLines(consoleLog)
        log.info { "runtimeClasspath 의존성 라인 수: ${runtimeClasspathLines.size}" }
        val runtimeDependencies = parseDependencies(runtimeClasspathLines, scope = "런타임")

        // 두 리스트 병합 및 scope 통합
        val dependencies = mergeDependencies(compileDependencies, runtimeDependencies)
        log.info { "병합된 의존성 개수: ${dependencies.size}" }

        // 전역 필터 적용
        val globalFilteredDeps = dependencies.filter { dep ->
            globalFilter?.invoke(dep) ?: true
        }

        // null 필터 섹션이 최대 1개인지 검증
        val nullFilterSections = sections.filter { it.filter == null }
        require(nullFilterSections.size <= 1) { "필터가 null인 섹션은 최대 1개만 허용됩니다." }

        // 필터가 있는 섹션과 null 필터 섹션 분리
        val normalSections = sections.filter { it.filter != null }
        val catchAllSection = nullFilterSections.firstOrNull()

        // 전체에서 선택된 의존성 추적 (전역)
        val globalSelectedDeps = mutableSetOf<Triple<String, String, String>>()

        // 섹션별 의존성 계산 및 캐싱
        val sectionDepsMap = mutableMapOf<DependencySection, List<DependencyInfo>>()

        // 먼저 필터가 있는 모든 섹션에서 선택된 의존성을 추적
        normalSections.forEach { section ->
            val sectionDeps = globalFilteredDeps.filter { dep ->
                section.filter!!(dep)
            }
            sectionDepsMap[section] = sectionDeps

            // 선택된 의존성을 그룹핑 키로 추적
            val uniqueKeys = sectionDeps.map { dep -> Triple(dep.groupId, dep.artifactId, dep.version) }.toSet()
            globalSelectedDeps.addAll(uniqueKeys)

            log.debug { "섹션 '${section.title}': ${uniqueKeys.size}개 고유 의존성 선택됨" }
        }

        // null 필터 섹션의 의존성 계산 (모든 필터를 통과하지 못한 것들)
        catchAllSection?.let { section ->
            log.debug { "null 필터 섹션 처리 전 전체 선택된 의존성: ${globalSelectedDeps.size}개" }

            val remainingDeps = globalFilteredDeps.filter { dep ->
                val key = Triple(dep.groupId, dep.artifactId, dep.version)
                !globalSelectedDeps.contains(key)
            }
            sectionDepsMap[section] = remainingDeps

            log.debug { "섹션 '${section.title}': ${remainingDeps.size}개 의존성 (모든 필터 통과 못함)" }
        }

        // 파일별로 섹션 그룹핑
        val sectionsByFile = sections.groupBy { it.fileName }

        // 파일별로 처리
        sectionsByFile.forEach { (fileName, fileSections) ->
            val sectionFile = File(rootDir, fileName)

            // 파일에 포함될 모든 섹션의 마크다운 생성
            val sectionMarkdowns = fileSections.mapNotNull { section ->
                val sectionDeps = sectionDepsMap[section] ?: emptyList()
                val sectionMd = generateSectionMarkdown(section.title, sectionDeps)
                if (sectionMd.isNotEmpty()) sectionMd else null
            }

            // 모든 섹션이 비어있으면 파일 생성하지 않음
            if (sectionMarkdowns.isEmpty()) {
                log.info { "파일 생성 생략 (모든 섹션이 비어있음): $fileName" }
                return@forEach
            }

            val mdContent = sectionMarkdowns.joinToString("\n\n")

            // 파일 저장
            sectionFile.writeText(mdContent, Charsets.UTF_8)

            val totalItems = fileSections.sumOf { section ->
                sectionDepsMap[section]?.size ?: 0
            }

            log.info { "파일 생성: ${sectionFile.absolutePath} (${sectionMarkdowns.size}개 섹션, 총 ${totalItems}개 항목)" }
        }

        log.info { "모든 섹션 파일 생성 완료" }
    }

    /**
     * 단일 섹션의 마크다운 생성
     */
    private fun generateSectionMarkdown(title: String, dependencies: List<DependencyInfo>): String {
        // groupId, artifactId, version으로 그룹핑 후 정렬
        val groupedDeps = dependencies
            .groupBy { dep -> Triple(dep.groupId, dep.artifactId, dep.version) }
            .toList()
            .sortedWith(compareBy({ it.first.first }, { it.first.second }))  // groupId -> artifactId 순 정렬

        // 필터링된 데이터 행 먼저 생성
        val dataRows = buildList {
            groupedDeps.forEach { (key, deps) ->
                val (groupId, artifactId, version) = key
                val scope = deps.first().scope

                // 출처 추출 (빈 문자열 제외)
                val sourcePaths = deps
                    .map { it.getSourcePath() }
                    .filter { it.isNotEmpty() }
                    .distinct()

                // project로 시작하지 않는 출처만 필터링
                val nonProjectSources = sourcePaths.filter { !it.startsWith("project:") }

                // 원래 출처가 없거나(root 의존성), 필터링 후에도 남은 출처가 있으면 표시
                if (sourcePaths.isEmpty() || nonProjectSources.isNotEmpty()) {
                    val sourcePathsStr = nonProjectSources.joinToString("<br>")
                    add("| $scope | $groupId | $artifactId | $version | $sourcePathsStr |")
                }
            }
        }

        // 데이터가 없으면 빈 문자열 반환
        if (dataRows.isEmpty()) {
            return ""
        }

        return buildString {
            // 섹션 제목
            appendLine("## $title")
            appendLine()

            // 헤더
            appendLine("| scope | 그룹 | 이름 | 버전 | 출처 |")
            appendLine("|-------|------|------|------|------|")

            // 데이터 행 추가
            dataRows.forEach { appendLine(it) }
        }
    }
}
