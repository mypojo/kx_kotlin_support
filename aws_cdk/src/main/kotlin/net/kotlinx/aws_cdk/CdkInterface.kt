package net.kotlinx.aws_cdk


/** 공용 로직 */
interface CdkInterface {

    /** CDK에 사용할 논리적 이름을 리턴한다. 동적으로 변경 가능 */
    val logicalName:String

}
