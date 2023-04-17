//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
}

//==================================================== 공통 ======================================================

/** 그래들 표준 문법을 간단하게 변경해줌 */
operator fun ProviderFactory.get(name: String): String = this.gradleProperty(name).get()

//==================================================== 프로젝트별 설정 ======================================================

dependencies {
    implementation(kotlin("stdlib"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4") //이미 AWS 때문에 코투틴이 로드 되어야함

}

