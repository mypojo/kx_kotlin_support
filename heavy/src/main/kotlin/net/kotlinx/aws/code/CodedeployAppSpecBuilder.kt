package net.kotlinx.aws.code

import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.aws.sts.StsUtil
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.koson.toGsonData

/**
 * appSepc 만들기 매우 짜증남.. 왜 이렇게 했을까..
 */
class CodedeployAppSpecBuilder(val ecsDeployData: EcsDeployData) {

    fun build(): GsonData = obj {
        "version" to 1
        "Resources" to arr[
            obj {
                "TargetService" to obj {
                    "Type" to "AWS::ECS::Service"
                    "Properties" to obj {
                        "TaskDefinition" to "arn:aws:ecs:${ecsDeployData.region}:${StsUtil.ACCOUNT_ID}:task-definition/${ecsDeployData.taskDef}"
                        "LoadBalancerInfo" to obj {
                            "ContainerName" to ecsDeployData.containerName
                            "ContainerPort" to ecsDeployData.containerPort
                        }
                        "PlatformVersion" to "1.4.0"
                    }
                }
            }
        ]
        //후킹 설정  https://docs.aws.amazon.com/ko_kr/codedeploy/latest/userguide/reference-appspec-file-structure-hooks.html
        ecsDeployData.beforeAllowTraffic?.let {
            "Hooks" to arr[
                obj { "BeforeAllowTraffic" to it }
            ]
        }
    }.toGsonData()

}
