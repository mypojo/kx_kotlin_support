package net.kotlinx.awscdk.basic

import software.amazon.awscdk.Tags
import software.constructs.IConstruct

/**
 * 커스텀 태그 세트
 * 태그 시리즈가 언더스코어가 아니라서 이렇게 둠
 *  */
enum class TagSet {

    /** 사내 특정 그룹을 지정 */
    IamGroup,

    /**
     * 다수의 프로젝트가 있는경우
     * 보통 비용 산출용으로 사용됨
     *  */
    Project,

    /** 일반적으로 표시되는 리소스의 네이밍 */
    Name,

    /** 배포 타입 */
    DeploymentType,
    ;

    fun tag(target: IConstruct, value: String) {
        Tags.of(target).add(this.name, value)
    }

    fun tag(target: IConstruct, value: Enum<*>) {
        Tags.of(target).add(this.name, value.name)
    }

}