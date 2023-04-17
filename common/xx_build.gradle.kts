plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.6.21"
    id("io.kotest.multiplatform") version "5.0.3"
}

kotlin {
    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs.plus(listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn"))
                jvmTarget = "11"
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")

                showCauses = true
                showExceptions = true
                showStackTraces = true
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT

                showStandardStreams = false
            }
        }
    }
    js(IR) {
        binaries.executable()
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs.plus("-opt-in=kotlin.RequiresOptIn")
                sourceMap = true
            }
        }
        moduleName = "myPojoLib"
        browser {

            //웹팩 구성 https://kotlinlang.org/docs/js-project-setup.html#webpack-task
//            //이거 풀고 ?>
//            webpackTask {
//                output.libraryTarget = "commonjs2" //??
//            }

            //https://github.com/eggeral/kotlin-single-js-file-lib 이거 참고
            commonWebpackConfig {
                //cssSupport.enabled = true
                outputFileName = "main.js"
                //output!!.libraryTarget = "commonjs2" //??
                outputPath = File(buildDir, "processedResources/jvm/main/static")
            }
            distribution {
                //directory = File("$projectDir/my") //뭐 없는데?
                directory = File(buildDir, "my") //뭐 없는데?
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                dependsOn(commonMain)
                api(project(":multi2"))
                api("com.google.code.gson:gson:2.10") // 외부 의존성 없음.. 깔끔함.  300kb 이내 -> 추가 함 해봄

//                implementation("org.jetbrains.kotlin:kotlin-reflect")
//                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
//
//                implementation("org.springframework.boot:spring-boot-starter-validation")
//
//                implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
//                implementation("org.springframework:spring-jdbc")
//                implementation("io.r2dbc:r2dbc-h2")
//                runtimeOnly("com.h2database:h2")
//
//                implementation("org.springframework.boot:spring-boot-starter-webflux")
//                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm")
//                runtimeOnly("org.webjars.npm:todomvc-common")
//                runtimeOnly("org.webjars.npm:todomvc-app-css")

                //runtimeOnly("io.github.microutils:kotlin-logging-jvm")
            }
        }
        val jvmTest by getting {
            dependencies {
//                implementation("org.springframework.boot:spring-boot-starter-test")
//                implementation("io.kotest:kotest-runner-junit5")
//                implementation("io.kotest.extensions:kotest-extensions-spring")
            }
        }

        val jsMain by getting {
            dependencies {
                dependsOn(commonMain)

//                implementation("org.jetbrains.kotlinx:kotlinx-html-js")
//
//                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
//                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-legacy")
//                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
//                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
//                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
//                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui")
//
//                implementation("io.github.microutils:kotlin-logging-js")

                implementation(npm("uuid", "8.3.2"))
                implementation(npm("is-sorted", "1.0.5"))
            }
        }
        val jsTest by getting
    }
}

tasks {
    named<Copy>("jvmProcessResources") {
        dependsOn(getByName("jsBrowserDevelopmentWebpack"))
    }
}
