package net.kotlinx.awscdk

import net.kotlinx.aws.AwsConfig
import net.kotlinx.koin.Koins.koin
import net.kotlinx.system.DeploymentType


/**
 * 작성 표준
 * #1  생성자에는 load() 가능한 project와 이름 정도만필수입력.  나머지는 기본값 설정 ==> 옵션이 많다면 DSL 적용 / 인라인 가능하면 DSL 필요없음
 * #2. 타 스택에서 같이 사용되어야 하는 리소스는 반드시 공유 가능하게 선언 되어야함  ex) S3, IAM, SQS ..
 * #3. 그자리에서 생성해서 쓰거나 트리 구조인경우 생성자에 DSL 필수. ex) RDS,DDB, SFN ..
 *  -> 커스텀이 자주 필요한건 props block을 반드시 열어둘것!
 *
 *  주의!!
 *  load 할때,
 *  1. 같은 스택에서 create 한 객체인경우 -> load() 하지말고 그냥 사용
 *  2. 같은 스택에서 create 하지 않은 경우 -> load() 로 초기화 후 사용해야함 (cycle 에러남)
 *  */
interface CdkInterface {

    /**
     * CDK에 사용할 논리적 이름을 리턴한다. 동적으로 변경 가능
     * 커스텀 이름 지정가능 -> displayName
     *  */
    val logicalName: String

    //==================================================== 편의용 단축 도구 ======================================================

    /** 내부 간단 사용용 */
    val deploymentType: DeploymentType
        get() = DEPLOYMENT_TYPE

    /** 내부 간단 사용용 */
    val suff: String
        get() = SUFF

    /** 내부 간단 사용용 */
    val awsConfig: AwsConfig
        get() = AWS_CONFIG

    /** 내부 간단 사용용 */
    val project: CdkProject
        get() = PROJECT

    /** 내부 간단 사용용 */
    val projectName: String
        get() = PROJECT.projectName

    companion object {

        /** 배포 타입 */
        val DEPLOYMENT_TYPE: DeploymentType
            get() = koin<DeploymentType>()

        /** 배포 타입의 접미어 소문자 버전 */
        val SUFF: String
            get() = DEPLOYMENT_TYPE.name.lowercase()

        /** AWS 설정 */
        val AWS_CONFIG: AwsConfig
            get() = koin<AwsConfig>()

        /** 프로젝트 명 */
        val PROJECT: CdkProject
            get() = koin<CdkProject>()

    }

}

/**
 * 다른데서 다수 참조되기 때문에 설정 형식으로 미리 정의되는것들
 * ex) S3,IAM,등등..
 * */
interface CdkEnum : CdkInterface

/**
 * 사용자 정의 네이밍 지원
 * displayName 을 logicalName과 다르게 세팅하고싶을때만 사용
 *
 * logicalName 이 다수 등록되어야 하는 경우라면 logicalName 생성에 name 을 필수로 지정해야함!
 * */
interface CdkCdkInterfaceName : CdkInterface {

    /** 커스텀하게 설정하는 이름 */
    var configuredName: String?

    /**
     * 실제 UI등에 보이는 디스플레이 이름
     * 설정이 없으면 logicalName 을 사용함
     *  */
    val displayName: String
        get() = configuredName ?: logicalName

}
