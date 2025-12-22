package net.kotlinx.gradle

import net.kotlinx.gradle.GradleDependencyToMd.DependencySection
import net.kotlinx.string.startsWithAny

object DependencySections {

    fun getDefaultSections(): List<DependencySection> = listOf(
        //==================================================== AWS ======================================================
        DependencySection(
            title = "AWS SDK",
            fileName = "dependencies_aws.md",
            filter = { dep -> dep.groupId.startsWith("aws.sdk.") }
        ),
        DependencySection(
            title = "AWS SDK 의존성",
            fileName = "dependencies_aws.md",
            filter = { dep -> dep.groupId.startsWith("aws.smithy.") }
        ),
        DependencySection(
            title = "AWS SDK (old)",
            fileName = "dependencies_aws.md",
            filter = { dep -> dep.groupId.startsWith("software.amazon.") }
        ),

        //==================================================== 구글 ======================================================
        DependencySection(
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
        DependencySection(
            title = "젯브레인 ktor",
            fileName = "dependencies_jetbrains.md",
            filter = { dep -> dep.groupId.startsWith("io.ktor") }
        ),
        DependencySection(
            title = "젯브레인 기타",
            fileName = "dependencies_jetbrains.md",
            filter = { dep -> dep.groupId.startsWith("org.jetbrains") }
        ),

        //==================================================== standard ======================================================
        DependencySection(
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
        DependencySection(
            title = "사실상 표준",
            fileName = "dependencies_standard.md",
            filter = { dep ->
                dep.groupId.startsWithAny(
                    setOf(
                        "joda-time", //구버전
                        "ch.qos.logback",
                        "org.slf4j",
                        "org.junit",
                        "junit",
                        "io.netty",
                        "com.squareup.",
                        "com.slack.",
                        "com.mysql",
                        "org.postgresql",
                        "com.zaxxer", //히카리
                        "io.swagger.core.",
                    )
                )
            }
        ),
        DependencySection(
            title = "사실상 표준(코틀린)",
            fileName = "dependencies_standard.md",
            filter = { dep ->
                dep.groupId.startsWithAny(
                    setOf(
                        "com.github.doyaaaaaken", //CSV
                        "io.insert-koin",
                    )
                )
            }
        ),
        DependencySection(
            title = "사실상 표준(스프링/하이버네이트)",
            fileName = "dependencies_standard.md",
            filter = { dep ->
                dep.groupId.startsWithAny(
                    setOf(
                        "org.springframework",
                        "org.hibernate",
                        "org.jboss",
                    )
                )
            }
        ),
        DependencySection(
            title = "사실상 표준의 의존성",
            fileName = "dependencies_standard.md",
            filter = { dep ->
                dep.groupId.startsWithAny(
                    setOf(
                        "com.fasterxml.jackson",
                        "commons-codec",
                        "com.lectra",
                        "net.bytebuddy",
                        "org.yaml",
                    )
                )
            }
        ),
        DependencySection(
            title = "아파치/커먼스 시리즈",
            fileName = "dependencies_standard.md",
            filter = { dep ->
                dep.groupId.startsWithAny(
                    setOf(
                        "org.apache.",
                        "commons-",
                    )
                )
            }
        ),

        //==================================================== 기타 ======================================================
        DependencySection(
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
        DependencySection(
            title = "기타",
            fileName = "dependencies_etc.md",
            filter = null  // null 필터: 다른 섹션에서 선택되지 않은 모든 데이터
        ),
    )
}
