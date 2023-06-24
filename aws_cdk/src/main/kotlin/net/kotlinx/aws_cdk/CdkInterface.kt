package net.kotlinx.aws_cdk


/**
 * 작성 표준
 * #1  생성자에는 load() 가능한 project와 이름 정도만필수입력.  나머지는 기본값 설정 ==> 옵션이 많다면 DSL 적용 / 인라인 가능하면 DSL 필요없음
 * #2. 타 스택에서 같이 사용되어야 하는 리소스는 반드시 공유 가능하게 선언 되어야함  ex) S3, IAM, SQS ..
 * #3. 그자리에서 생성해서 쓰거나 트리 구조인경우 생성자에 DSL 필수. ex) RDS,DDB, SFN ..
 *  -> 커스텀이 자주 필요한건 props block을 반드시 열어둘것!
 *  */
interface CdkInterface {

    /** CDK에 사용할 논리적 이름을 리턴한다. 동적으로 변경 가능 */
    val logicalName: String

}
