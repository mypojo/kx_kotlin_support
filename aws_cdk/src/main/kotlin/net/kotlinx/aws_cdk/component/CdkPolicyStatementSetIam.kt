package net.kotlinx.aws_cdk.component

/**
 *  자주 사용되는 PolicyStatement 모음
 *  */
object CdkPolicyStatementSetIam {


    /**
     * 일단 MFA 는 수동으로..
     * */
    fun userDefault(awsId:String): CdkPolicyStatement = CdkPolicyStatement {
        actions = listOf(
            "iam:ListUsers",      //리스팅은 해야 진입하지..
            "iam:ChangePassword", //셀프 비번 변경
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

    /**
     *
     * {
     *     "Version": "2012-10-17",
     *     "Statement": [
     *         {
     *             "Sid": "VisualEditor0",
     *             "Effect": "Allow",
     *             "Action": [
     *                 "iam:DeleteAccessKey",
     *                 "iam:GetAccessKeyLastUsed",
     *                 "iam:UpdateAccessKey",
     *                 "iam:CreateAccessKey",
     *                 "iam:ListAccessKeys"
     *             ],
     *             "Condition": {
     *                 "BoolIfExists": {
     *                     "aws:MultiFactorAuthPresent": "true"
     *                 }
     *             }
     *         }
     *     ]
     * }
     * */


}
