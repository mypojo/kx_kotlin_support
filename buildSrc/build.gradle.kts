plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    repositories {
        maven { url = uri("https://repo.repsy.io/mvn/mypojo/kotlin_support") }
    }
    dependencies {
        /** 셀프 참조로 가져온다. 버전 정보는 하드코딩 */
        val kotlinSupportVersionForBuild = "2024-08-13"
        implementation("net.kotlinx.kotlin_support:heavy:${kotlinSupportVersionForBuild}")
    }
}