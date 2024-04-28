package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.koin.Koins

/**
 *  자주 사용되는 PolicyStatement 모음
 *  */
object CdkPolicyStatementSetIam {

    /**
     * 기본적으로 AWS 콘솔에 들어와서 작업할 수 있는 기본기능 제공
     * 일단 MFA 는 수동으로..
     * */
    fun userDefault(awsId: String = Koins.get<CdkProject>().awsId): CdkPolicyStatement = CdkPolicyStatement {
        actions = listOf(
            "iam:ChangePassword", //셀프 비번 변경 (최초 로그인 OR 직접수정)
            "iam:ListUsers",      //리스팅은 해야 진입하지..
            //이하 키 발급
            "iam:DeleteAccessKey",
            "iam:GetAccessKeyLastUsed",
            "iam:UpdateAccessKey",
            "iam:CreateAccessKey",
            "iam:ListAccessKeys",
        )
        resources = listOf(
            "arn:aws:iam::${awsId}:user/\${aws:username}" //$ 이스케이핑됨 주의!!
        )
    }


}
