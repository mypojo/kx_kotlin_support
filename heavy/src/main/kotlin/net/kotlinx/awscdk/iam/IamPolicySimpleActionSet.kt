package net.kotlinx.awscdk.iam


/**
 * 리소스 사용의 최소 역할 부여
 * 보통 리소스 역할 할당에 사용
 * */
object IamPolicySimpleActionSet {

    val SQS = listOf("sqs:SendMessage")

}