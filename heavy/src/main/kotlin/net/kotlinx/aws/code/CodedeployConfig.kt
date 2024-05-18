package net.kotlinx.aws.code

/**
 * https://docs.aws.amazon.com/ko_kr/codedeploy/latest/userguide/deployment-configurations.html 참고
 * 그 자체로 이름이 매핑되기 때문에 언더스코어 형식으로 변경하지 않음
 */
enum class CodedeployConfig {

    ECSAllAtOnce,
    ECSLinear10PercentEvery1Minutes;

    /** 실제 설정 이름 */
    fun toConfig(): String = "CodeDeployDefault.$name"
}