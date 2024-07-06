package net.kotlinx.aws.code

import net.kotlinx.aws.AwsConfig
import net.kotlinx.core.Kdsl

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

    /** ??
     * 참고로 이미지 주소는 이미 있음..
     * */
    lateinit var containerName: String

    /**
     * 리비전 번호가 포함되지 않으면  latest 인듯.
     * ex) dd-web_task_def-dev:27
     * */
    lateinit var taskDef: String

    /**
     * 자주 사용하는 후킹 정보 (승인처리 등)
     * */
    var beforeAllowTraffic: String? = null

    /** 컨테이너 포트 */
    var containerPort: Int = 8080

    /** 리즌 */
    lateinit var awsConfig: AwsConfig
//    /** 리즌 */
//    var region: String = AwsConfig.SEOUL

    /** ?? */
    lateinit var applicationName: String

    /** ?? */
    lateinit var deploymentGroupName: String

    /** 코드 디플로이 교체 설정 */
    var codedeployConfig = CodedeployConfig.ECSAllAtOnce


}