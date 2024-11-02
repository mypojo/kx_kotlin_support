package net.kotlinx.aws.codeDeploy

import net.kotlinx.aws.AwsConfig
import net.kotlinx.core.Kdsl
import net.kotlinx.koin.Koins.koin

/**
 * 디플로이용 공통 설정데이터
 */
class EcsDeployData {

    @Kdsl
    constructor(block: EcsDeployData.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 롤링 배포 ======================================================

    /** 클러스터 이름 */
    lateinit var clusterName: String

    /** 클러스터의 서비스 이름 */
    lateinit var serviceName: String


    //==================================================== 블루그린 배포 ======================================================

    /**
     * 리비전 번호가 포함되지 않으면  latest 인듯.
     * ex) dd-web_task_def-dev:27
     * */
    lateinit var taskDef: String

    /**
     * taskDef 안에 다수의 컨테이너 설정가능. (보통1개)
     * ex) xxx-web_container-prod
     * */
    lateinit var containerName: String

    /**
     * https://docs.aws.amazon.com/ko_kr/codedeploy/latest/userguide/reference-appspec-file-structure-hooks.html
     * 자주 사용하는 후킹 정보 (승인처리 등)
     * 람다 이름을 넣으면 됨
     * */
    var beforeAllowTraffic: String? = null

    /** 컨테이너 포트 */
    var containerPort: Int = 8080

    /** 리즌 */
    var awsConfig: AwsConfig = koin<AwsConfig>()

    //==================================================== 코드 디플로이 설정 ======================================================

    /**
     * 코드 디플로이 -> Applications
     * ex) xx-web_codedeploy-prod
     * */
    lateinit var codedeployApplicationName: String

    /**
     * 코드 디플로이 내부의 그룹
     * ex) xx-web_codedeploy_group-prod
     * */
    lateinit var codedeployDeploymentGroupName: String

    /** 코드 디플로이 교체 설정 */
    var codedeployConfig = CodedeployConfig.ECSAllAtOnce


}