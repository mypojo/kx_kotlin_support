package net.kotlinx.gradle

import net.kotlinx.file.slash
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.startsWithAny
import java.io.File

class DependenciesTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("DependenciesTest") {

            Then("의존성 분석") {

                val root = File("C:\\Users\\mypoj\\IdeaProjects\\kx_kotlin_support\\light\\dependencies")
                val consoleLogFile = root.slash("console.txt")

                // GradleDependencyToMd 사용
                val converter = GradleDependencyToMd()

                // 섹션 정의 (파일별로 분리)
                val sections = listOf(
                    //==================================================== AWS ======================================================
                    GradleDependencyToMd.DependencySection(
                        title = "AWS",
                        fileName = "dependencies_aws.md",
                        filter = { dep -> dep.groupId.startsWith("com.amazonaws") }
                    ),
                    GradleDependencyToMd.DependencySection(
                        title = "AWS SDK",
                        fileName = "dependencies_aws.md",
                        filter = { dep -> dep.groupId.startsWith("aws.") }
                    ),
                    GradleDependencyToMd.DependencySection(
                        title = "AWS SDK (old)",
                        fileName = "dependencies_aws.md",
                        filter = { dep -> dep.groupId.startsWith("software.amazon.") }
                    ),

                    //==================================================== 구글 ======================================================
                    GradleDependencyToMd.DependencySection(
                        title = "구글",
                        fileName = "dependencies_google.md",
                        filter = { dep ->
                            dep.groupId.startsWithAny(
                                setOf(
                                    "com.google.",
                                    "io.grpc",
                                )
                            )
                        }
                    ),

                    //==================================================== 젯브레인 ======================================================
                    GradleDependencyToMd.DependencySection(
                        title = "젯브레인 ktor",
                        fileName = "dependencies_jetbrains.md",
                        filter = { dep -> dep.groupId.startsWith("io.ktor") }
                    ),
                    GradleDependencyToMd.DependencySection(
                        title = "젯브레인 기타",
                        fileName = "dependencies_jetbrains.md",
                        filter = { dep -> dep.groupId.startsWith("org.jetbrains") }
                    ),

                    //==================================================== standard ======================================================
                    GradleDependencyToMd.DependencySection(
                        title = "JVM 표준",
                        fileName = "dependencies_standard.md",
                        filter = { dep ->
                            dep.groupId.startsWithAny(
                                setOf(
                                    "javax.", //구버전
                                    "jakarta.",
                                )
                            )
                        }
                    ),
                    GradleDependencyToMd.DependencySection(
                        title = "사실상 표준",
                        fileName = "dependencies_standard.md",
                        filter = { dep ->
                            dep.groupId.startsWithAny(
                                setOf(
                                    "joda-time", //구버전
                                    "ch.qos.logback",
                                    "org.slf4j",
                                    "org.junit",
                                    "io.netty",
                                    "com.squareup.",
                                    "com.slack.",

                                    "com.github.doyaaaaaken", //CSV
                                    "io.insert-koin", //CSV
                                )
                            )
                        }
                    ),
                    GradleDependencyToMd.DependencySection(
                        title = "사실상 표준의 의존성",
                        fileName = "dependencies_standard.md",
                        filter = { dep ->
                            dep.groupId.startsWithAny(
                                setOf(
                                    "com.fasterxml.jackson",
                                    "commons-codec",
                                )
                            )
                        }
                    ),
                    GradleDependencyToMd.DependencySection(
                        title = "아파치 시리즈",
                        fileName = "dependencies_standard.md",
                        filter = { dep ->
                            dep.groupId.startsWithAny(
                                setOf(
                                    "org.apache.",
                                )
                            )
                        }
                    ),

                    //==================================================== 기타 ======================================================
                    GradleDependencyToMd.DependencySection(
                        title = "AI 관련",
                        fileName = "dependencies_etc.md",
                        filter = { dep ->
                            dep.groupId.startsWithAny(
                                setOf(
                                    "com.aallam.openai",
                                    "io.modelcontextprotocol",
                                )
                            )
                        }
                    ),
                    GradleDependencyToMd.DependencySection(
                        title = "기타",
                        fileName = "dependencies_etc.md",
                        filter = null  // null 필터: 다른 섹션에서 선택되지 않은 모든 데이터
                    ),
                )

                // org.jetbrains로 시작하는 의존성 제외 (전역 필터)
                val ignores = setOf("project")
                val globalFilter: (GradleDependencyToMd.DependencyInfo) -> Boolean = { dep ->
                    !dep.groupId.startsWithAny(ignores)
                }

                converter.convertToMarkdownWithSections(
                    consoleLogFile = consoleLogFile,
                    rootDir = root,
                    sections = sections,
                    globalFilter = globalFilter
                )

            }

        }
    }

}