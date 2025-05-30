package net.kotlinx.awscdk.iam

/**
 *  자주 사용되는 PolicyStatement 모음
 *  */
object CdkPolicyStatementSetAthena {

    /**
     * 아테나 쿼시실행 전체권한
     * 여기에 추가 접근 권한이 있어야 실제 실행 가능함
     *  */
    val ATHENA_EXE_ALL = CdkPolicyStatement {
        actions = listOf(
            "athena:BatchGetQueryExecution",
            "athena:GetQueryExecution",
            "athena:GetQueryExecutions",
            "athena:GetQueryResults",
            "athena:GetQueryResultsStream",

            "athena:StartQueryExecution",
            "athena:StopQueryExecution",
            "athena:CancelQueryExecution",

            "athena:RunQuery", //레거시에서 쓰던 권한. StartQueryExecution으로 대체되었음
            "athena:ListQueryExecutions",
            //아테나 네임드쿼리
            "athena:ListNamedQueries",
            "athena:GetNamedQuery",
            "athena:BatchGetNamedQuery",
        )
    }

    val ATHENA_READONLY = CdkPolicyStatement {
        actions = listOf(
            "athena:StartQueryExecution",
            "athena:StopQueryExecution",
            "athena:Get*",
            "athena:BatchGet*",
            "athena:List*",
        )
    }

    val GLUE_READONLY = CdkPolicyStatement {
        actions = listOf(
            "glue:Get*",
            "glue:BatchGet*"
        )
    }

}
