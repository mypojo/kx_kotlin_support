package net.kotlinx.awscdk

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

    /** CDK에 사용할 논리적 이름을 리턴한다. 동적으로 변경 가능 */
    val logicalName: String

    val project: CdkProject
        get() = koin<CdkProject>()

    val deploymentType: DeploymentType
        get() = koin<DeploymentType>()

}

/**
 * 다른데서 다수 참조되기 때문에 설정 형식으로 미리 정의되는것들
 * ex) S3,IAM,등등..
 * */
interface CdkEnum : CdkInterface
