package net.kotlinx.awscdk


/**
 * 하나의 AWS 계정에서 다수의 프로젝트를 운영할 수 있음
 * 이경우 사용
 *  */
data class CdkProject(

    /** 내부 프로젝트 이름 */
    val projectName: String

)
