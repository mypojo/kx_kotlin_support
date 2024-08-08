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
                .catalogId(CdkInterface.PROJECT.awsId)
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
                        .build()
                )
                .build()
        )
        TagUtil.tag(database, CdkInterface.DEPLOYMENT_TYPE)
    }
}