package net.kotlinx.core

/**
 * 패키지 네임 기록
 * 향후 모킹할때, 특정 확장함수를 사용하기 위함
 * ex) object StringNumberSupport : PackageNameSupport
 *  */
interface PackageNameSupport {

    /**
     * 모킹용 이름을 리턴함
     * 컴파일되면 이름 변경됨 StringNumberSupport.kt -> StringNumberSupportKt
     * */
    fun packageName(): String = "${this::class.qualifiedName}Kt"
}