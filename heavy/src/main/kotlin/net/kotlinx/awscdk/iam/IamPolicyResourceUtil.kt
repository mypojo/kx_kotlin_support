package net.kotlinx.awscdk.iam

import software.amazon.awscdk.services.iam.AccountPrincipal
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.PolicyStatement


/**
 * 리소스 베이스의 폴리시 생성
 * */
object IamPolicyResourceUtil {


    /**
     * 역할 부여 샘플,  addToResourcePolicy 로 부여하면됨
     * @param resources
     *  arn:aws:sqs:ap-northeast-2:xxx:yyy.fifo
     * @param actions
     *  sqs:SendMessage
     * */
    fun forAccount(awsIds: List<String>, resources: List<String>, actions: List<String>): PolicyStatement {
        return PolicyStatement.Builder.create()
            .effect(Effect.ALLOW)
            .principals(awsIds.map { AccountPrincipal(it) })
            .actions(actions)
            .resources(resources)
            .build()
    }


}