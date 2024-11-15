package net.kotlinx.awscdk.data

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.glue.CfnDatabase
import software.amazon.awscdk.services.glue.CfnDatabaseProps

/**
 * 아테나, 글루, 데이터레이크용
 * */
@Deprecated("권한이슈 많음!! 가능하면 SDK 사용")
class CdkGlueDatabase : CdkInterface {

    @Kdsl
    constructor(block: CdkGlueDatabase.() -> Unit = {}) {
        apply(block)
    }

    /** DB 명 */
    override val logicalName: String
        get() = "glue_db_${databaseName}-${suff}"

    /** 데이터베이스명 */
    lateinit var databaseName: String

    /**
     * from 데이터베이스명
     * 다른데서 쉐어(RAM)한 데이터베이스를 내가 읽을수 있게 DB링크로 만들어줄때 사용
     * <catalogId/databaseName>
     *  */
    var link: Pair<String, String>? = null

    /** 결과 */
    lateinit var database: CfnDatabase

    /** 데이터베이스와 워크 그룹을 만들어준다 */
    fun create(stack: Stack) {
        database = CfnDatabase(
            stack, logicalName, CfnDatabaseProps.builder()
                .catalogId(CdkInterface.AWS_CONFIG.awsId)
                .databaseInput(
                    CfnDatabase.DatabaseInputProperty.builder()
                        .name(databaseName)
                        .description(if (link == null) "database" else "database link ${link!!.first}:${link!!.second}")
                        .apply {
                            link?.let {
                                targetDatabase(
                                    CfnDatabase.DatabaseIdentifierProperty.builder()
                                        .catalogId(it.first)
                                        .databaseName(it.second)
                                        .build()
                                )
                            }
                        }
//                        .createTableDefaultPermissions(
//                            listOf(
//                                PrincipalPrivilegesProperty.builder()
//                                    .permissions(listOf("CREATE_TABLE","DESCRIBE"))
//                                    .principal(DataLakePrincipalProperty.builder().dataLakePrincipalIdentifier("arn:aws:iam::xxx:role/xxx").build())
//                                    .build()
//                            )
//                        )
                        .build()
                )
                .build()
        )
        TagUtil.tag(database, CdkInterface.DEPLOYMENT_TYPE)
    }
}