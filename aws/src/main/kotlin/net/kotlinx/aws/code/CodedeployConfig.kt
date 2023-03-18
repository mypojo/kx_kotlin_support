package net.kotlinx.aws.code

/**
 * https://docs.aws.amazon.com/ko_kr/codedeploy/latest/userguide/deployment-configurations.html 참고
 */
enum class CodedeployConfig {

    ECSAllAtOnce,
    ECSLinear10PercentEvery1Minutes;

    fun toConfig(): String = "CodeDeployDefault.$name"
}