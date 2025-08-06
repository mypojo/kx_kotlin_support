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
        val kotlinSupportVersionForBuild = "2025-04-01"
        implementation("net.kotlinx.kotlin_support:heavy:${kotlinSupportVersionForBuild}")

        //api("com.squareup.okhttp3:okhttp:_") //gradle 에서 슬랙알람 보낼때 NoSuchMethodError 회피 -> 일단 보류
    }
}