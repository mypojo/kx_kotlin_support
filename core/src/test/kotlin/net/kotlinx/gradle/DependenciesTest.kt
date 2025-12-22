package net.kotlinx.gradle

import net.kotlinx.file.slash
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.startsWithAny
import net.kotlinx.system.ResourceHolder

class DependenciesTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("DependenciesTest") {

            Then("의존성 분석") {
                val projects = listOf("core", "light", "heavy","work")
                projects.forEach { project ->
                    val root = ResourceHolder.USER_ROOT.slash("IdeaProjects").slash("kx_kotlin_support").slash(project).slash("dependencies")
                    val consoleLogFile = root.slash("console.txt")

                    // 섹션 정의는 DependencySections 객체에서 가져옴
                    val sections = DependencySections.getDefaultSections()

                    // org.jetbrains로 시작하는 의존성 제외 (전역 필터)
                    val ignores = setOf("project")
                    val globalFilter: (GradleDependencyToMd.DependencyInfo) -> Boolean = { dep ->
                        !dep.groupId.startsWithAny(ignores)
                    }

                    // GradleDependencyToMd 사용
                    val converter = GradleDependencyToMd()
                    converter.convertToMd(
                        consoleLogFile = consoleLogFile,
                        rootDir = root,
                        sections = sections,
                        globalFilter = globalFilter
                    )
                }
            }
        }
    }

}