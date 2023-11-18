package net.kotlinx.aws_cdk.module

import net.kotlinx.aws.athena.AthenaTable
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.component.*
import net.kotlinx.aws_cdk.util.IamCertType
import net.kotlinx.aws_cdk.util.TagSet
import net.kotlinx.core.Kdsl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import software.amazon.awscdk.SecretValue
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.athena.CfnWorkGroup
import software.amazon.awscdk.services.athena.CfnWorkGroupProps
import software.amazon.awscdk.services.iam.Group
import software.amazon.awscdk.services.iam.GroupProps
import software.amazon.awscdk.services.iam.User
import software.amazon.awscdk.services.iam.UserProps


/**
 * athena 테이블 쿼리를 외부 인원에게 노출하고싶을때 사용함
 * 테이블별 권한부여, 비용분리 등의 기능 포함
 * 권한 샘플 참고용
 * */
class AthenaIamUser : KoinComponent {

    @Kdsl
    constructor(block: AthenaIamUser.() -> Unit = {}) {
        apply(block)
    }

    /** 팀 이름 (IAM 그룹으로 만들어짐) */
    lateinit var teamName: String

    /** 결과가 저장될 버킷 (S3 key 네이밍은 고정) */
    lateinit var resultBucket: String

    /** 워크그룹에 할당할 스캔 컷오프 */
    var bytesScannedCutoffPerQuery: Long = CdkAthena.GB_TO_BYTE * 1

    /**
     * 권한을 줄 테이블들
     * */
    lateinit var tables: List<AthenaTable>

    /**
     * 고정된 기본 권한
     * 로그인 필요시 CdkPolicyStatementSetIam.userDefault(MyEnv.AWS_ID) 등 추가
     *  */
    var defaultStatements = listOf(
        CdkPolicyStatementSetAthena.ATHENA_EXE_ALL,
    )

    /** 임시 비밀번호 접미어 */
    var tempPwdSuff: String = "123!"

    /** 계정 생성 타입 */
    var iamCertType: IamCertType = IamCertType.USER

    /**
     * 생성할 사용자 리스트
     *  */
    lateinit var userNames: List<String>

    val workgroupName: String
        get() = "workgroup-${teamName}"

    /**
     * 아테나 & 글루 종합 권한
     * 여기서 resources로 적절히 필터링 해야 테이블 리스트나 워크 그룹에  권한없는 객체가 안보임
     * database나 table 등은 태그를 못달기 때문에 태그 베이스로 권한을 부여하지 않음
     * 태그 적용시 aws:PrincipalTag 를 사용할것
     *  */
    private fun createAthenaTablesStatement(): CdkPolicyStatement {
        val project: CdkProject by inject()
        return CdkPolicyStatement {
            actions = listOf(
                "glue:BatchGetPartition",
                "glue:GetDatabase",
                "glue:GetDatabases",
                "glue:GetPartition",
                "glue:GetPartitions",
                "glue:GetTables",
                "glue:GetTable",
                //데이터베이스 및 테이블
                "athena:GetCatalogs",
                "athena:GetDataCatalog",
                "athena:GetDatabase",
                "athena:GetExecutionEngine",
                "athena:GetExecutionEngines",
                "athena:GetNamespace",
                "athena:GetNamespaces",
                "athena:ListEngineVersions",
                "athena:ListDataCatalogs",
                "athena:ListDatabases",
                "athena:ListTableMetadata",
                "athena:GetTable",
                "athena:GetTableMetadata",
                "athena:GetTables",
                //워크그룹
                "athena:ListWorkGroups",
                "athena:GetWorkGroup",
            )
            val resourceCatalog = listOf("arn:aws:glue:${project.region}:${project.awsId}:catalog")
            val resourceDatabase = tables.map { it.database }.distinct().map { "arn:aws:glue:${project.region}:${project.awsId}:database/${it}" }
            val resourceTables = tables.map { "arn:aws:glue:${project.region}:${project.awsId}:table/${it.database}/${it.tableName}" }
            val resourceWorkGroup = listOf("arn:aws:athena:${project.region}:${project.awsId}:workgroup/${workgroupName}")
            resources = resourceCatalog + resourceDatabase + resourceTables + resourceWorkGroup
        }
    }

    fun create(stack: Stack) {

        val outputDir = "${resultBucket}/athena/${teamName}"

        //==================================================== 아테나 워크구룹 생성 ======================================================
        val workGroup = CfnWorkGroup(
            stack, workgroupName, CfnWorkGroupProps.builder()
                .name(workgroupName)
                .description("workGroup for $teamName")
                .workGroupConfiguration(
                    CfnWorkGroup.WorkGroupConfigurationProperty.builder()
                        .bytesScannedCutoffPerQuery(bytesScannedCutoffPerQuery)
                        //결과가 상위 디렉토리에 저장되기 때문에 별도 workgroup에 권한을 제한할 필요가 없다 (보기엔 좀 그렇다)
                        .resultConfiguration(
                            CfnWorkGroup.ResultConfigurationProperty.builder()
                                .outputLocation("s3://${outputDir}/outputLocation").build()
                        )
                        .build()
                )
                .build()
        )
        TagSet.IamGroup.tag(workGroup, teamName) //워크스페이스에는 태깅 가능

        //==================================================== IAM 생성 ======================================================
        when (iamCertType) {
            IamCertType.USER -> {
                val policy = CdkIamPolicy {
                    policyName = "app_${teamName}_athena"

                    val tableBuckets = tables.map { it.bucket }.distinct().map { "arn:aws:s3:::${it}" }
                    val tablePaths = tables.map { "arn:aws:s3:::${it.bucket}/${it.s3Key}*" } //전체(*)를 지정해야함
                    val customStates = listOf(
                        CdkPolicyStatementSetS3.s3Read(tableBuckets + tablePaths), //테이블에 읽기 권한 부여
                        CdkPolicyStatementSetS3.s3ReadWrite(listOf("arn:aws:s3:::${outputDir}/*")), //쿼리 결과 저장소에 쓰기 권한 부여
                        createAthenaTablesStatement(),
                    )
                    statements = defaultStatements + customStates
                    create(stack)
                }

                //==================================================== IAM USER ======================================================
                val group = Group(
                    stack, "iam_group-$teamName", GroupProps.builder()
                        .groupName(teamName)
                        .managedPolicies(listOf(policy.iManagedPolicy))
                        .build()
                )

                userNames.forEach { userName ->
                    User(
                        stack, "iam_user-${userName}", UserProps.builder()
                            .groups(listOf(group))
                            .userName(userName)
                            .passwordResetRequired(true)
                            .password(SecretValue.unsafePlainText("${userName}${tempPwdSuff}"))
                            .build()
                    )
                }
            }

            IamCertType.ROLE -> throw UnsupportedOperationException("준비중..")
        }


    }

}
