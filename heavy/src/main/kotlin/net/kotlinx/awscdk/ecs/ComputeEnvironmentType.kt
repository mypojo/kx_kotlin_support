package net.kotlinx.awscdk.ecs

enum class ComputeEnvironmentType(val resourceType: String) {
    NORMAL("FARGATE"),
    SPOT("FARGATE_SPOT"),
    ;
}