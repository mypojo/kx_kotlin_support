package net.kotlinx.aws.code

import com.lectra.koson.ObjectType
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.aws.AwsConfig

/**
 * appSepc 만들기 매우 짜증남.. 왜 이렇게 했을까..
 */
data class CodedeployAppSpecBuilder(
    private val awsId: String,
    private val containerName: String,
    /** 리비전 번호가 포함되지 않으면  latest 인듯.  ex) dd-web_task_def-dev:27 */
    private val taskDef: String,
    private val lambdaHookName: String = "",
    private val containerPort: Int = 8080,
    private val region: String = AwsConfig.SEOUL,
) {
    fun build(): ObjectType = obj {
        "version" to 1
        "Resources" to arr[
                obj {
                    "TargetService" to obj {
                        "Type" to "AWS::ECS::Service"
                        "Properties" to obj {
                            "TaskDefinition" to "arn:aws:ecs:${region}:${awsId}:task-definition/${taskDef}"
                            "LoadBalancerInfo" to obj {
                                "ContainerName" to containerName
                                "ContainerPort" to containerPort
                            }
                            "PlatformVersion" to "1.4.0"
                        }
                    }
                }
        ]
        //후킹 설정  https://docs.aws.amazon.com/ko_kr/codedeploy/latest/userguide/reference-appspec-file-structure-hooks.html
        if (lambdaHookName.isNotEmpty()) {
            "Hooks" to arr[
                    obj { "BeforeAllowTraffic" to lambdaHookName }
            ]
        }
    }

}
