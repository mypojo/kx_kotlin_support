plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    repositories {
        maven { url = uri("https://repo.repsy.io/mvn/mypojo/kotlin_support") }
    }
    dependencies {
        //경고!! 코틀린 그래들 플러그인은 버전 업데이트가 늦어서, 과거 버번을 써야하는 경우가 많으니 업데이트에 주의할것!!
        val kotlinSupportVersionForBuild = "2025-04-01"  //최신 스테이블 버전 = "2025-04-01"
        implementation("net.kotlinx.kotlin_support:heavy:${kotlinSupportVersionForBuild}")

        //api("com.squareup.okhttp3:okhttp:_") //gradle 에서 슬랙알람 보낼때 NoSuchMethodError 회피 -> 일단 보류
    }
}